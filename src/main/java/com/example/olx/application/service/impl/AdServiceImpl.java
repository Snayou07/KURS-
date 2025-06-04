// src/main/java/com/example/olx/application/service/impl/AdServiceImpl.java
package com.example.olx.application.service.impl;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.application.service.port.AdServicePort;
// Unused import: com.example.olx.application.service.port.CategoryServicePort;
import com.example.olx.application.service.port.NotificationServicePort;

import com.example.olx.application.service.strategy.AdSearchStrategy;
import com.example.olx.domain.exception.AdNotFoundException;
import com.example.olx.domain.exception.InvalidInputException;
import com.example.olx.domain.exception.UnauthorizedActionException;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.AdState;
import com.example.olx.domain.model.ActiveAdState; // Required for instanceof check
import com.example.olx.domain.model.User;
import com.example.olx.domain.model.UserType;
import com.example.olx.domain.repository.AdRepository;
import com.example.olx.domain.repository.CategoryRepository;
import com.example.olx.domain.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
// Required for .toList() or explicit Collectors.toList()

public class AdServiceImpl implements AdServicePort {
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationServicePort notificationService;
    private final AdSearchStrategy searchStrategy;

    // Constructor
    public AdServiceImpl(AdRepository adRepository, UserRepository userRepository,
                         CategoryRepository categoryRepository,
                         NotificationServicePort notificationService, AdSearchStrategy searchStrategy) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.notificationService = notificationService;
        this.searchStrategy = searchStrategy;
    }

    @Override
    public void changeAdState(String adId, AdState newState) throws UserNotFoundException {
        // Validate input parameters
        if (adId == null || adId.trim().isEmpty()) {
            throw new InvalidInputException("Ad ID cannot be null or empty");
        }
        if (newState == null) {
            throw new InvalidInputException("AdState cannot be null");
        }

        // Find the ad by ID
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new AdNotFoundException("Ad with ID " + adId + " not found"));
        // Update the ad state
        ad.setCurrentState(newState);
        // Save the updated ad
        adRepository.save(ad);
        // Optional: Send notification about state change
        if (notificationService != null) {
            try {
                notificationService.notifyAdStateChanged(ad, newState);
            } catch (Exception e) {
                System.err.println("Error sending state change notification: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Ad> getAllActiveAds() {
        List<Ad> ads = adRepository.findAll();
        System.out.println("getAllActiveAds() initial count: " + ads.size() + " ads"); // DEBUG

        // Фільтруємо тільки активні оголошення для головного екрану
        List<Ad> activeAds = ads.stream()
                .filter(ad -> ad.getCurrentState() instanceof ActiveAdState) // MODIFIED: Check state type
                .collect(Collectors.toList());
        // Using .collect(Collectors.toList()) for wider Java compatibility than .toList()

        System.out.println("Active ads after filtering by instanceof ActiveAdState: " + activeAds.size()); // DEBUG
        return activeAds;
    }

    @Override
    public Ad createAd(AdCreationRequest request) throws UserNotFoundException {
        // Валідація вхідних даних
        if (request == null) {
            throw new InvalidInputException("AdCreationRequest не може бути null.");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new InvalidInputException("Назва оголошення є обов'язковою.");
        }

        if (request.getPrice() < 0) {
            throw new InvalidInputException("Ціна не може бути від'ємною.");
        }

        if (request.getCategoryId() == null || request.getCategoryId().trim().isEmpty()) {
            throw new InvalidInputException("ID категорії є обов'язковим.");
        }

        if (request.getSellerId() == null || request.getSellerId().trim().isEmpty()) {
            throw new InvalidInputException("ID продавця є обов'язковим.");
        }

        // Перевірка існування продавця
        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new UserNotFoundException("Продавець з ідентифікатором " + request.getSellerId() + " не знайдений."));
        // Перевірка існування категорії
        categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new InvalidInputException("Категорія з ідентифікатором " + request.getCategoryId() + " не знайдена."));
        try {
            // Створюємо нове оголошення з усіма параметрами
            Ad newAd = new Ad(
                    request.getTitle().trim(),
                    request.getDescription() != null ? request.getDescription().trim() : "",
                    request.getPrice(),
                    request.getCategoryId().trim(),
                    request.getSellerId().trim(),
                    request.getImagePaths()
            );
            // Автоматично публікуємо оголошення для відображення на головному екрані
            newAd.setCurrentState(new ActiveAdState());
            System.out.println("Creating ad: " + newAd.getTitle() + " with state: " + newAd.getCurrentState().getClass().getSimpleName()); // DEBUG

            // Зберігаємо оголошення в репозиторії
            Ad savedAd = adRepository.save(newAd);
            // Перевіряємо, що оголошення дійсно збережено
            if (savedAd == null) {
                throw new RuntimeException("Error saving ad to repository.");
            }

            System.out.println("Ad saved with ID: " + savedAd.getId()); // DEBUG
            System.out.println("Ad status after save (from getStatus()): " + savedAd.getStatus()); // DEBUG
            System.out.println("Ad state object after save: " + savedAd.getCurrentState().getClass().getSimpleName()); // DEBUG

            // Перевіряємо, чи з'являється оголошення в загальному списку
            List<Ad> allAdsCheck = adRepository.findAll(); // Renamed variable to avoid conflict
            System.out.println("Total ads after creation: " + allAdsCheck.size()); // DEBUG

            boolean adFound = allAdsCheck.stream().anyMatch(ad -> ad.getId().equals(savedAd.getId()));
            System.out.println("New ad found in full list: " + adFound); // DEBUG

            // Відправляємо сповіщення користувачам про нове оголошення
            try {
                if (notificationService != null) {
                    notificationService.notifyUsersAboutNewAd(savedAd, userRepository.findAll());
                }
            } catch (Exception e) {
                // Логування помилки сповіщення, але не зупиняємо процес створення оголошення
                System.err.println("Error sending new ad notifications: " + e.getMessage());
            }

            return savedAd;
        } catch (Exception e) {
            // Логування детальної інформації про помилку
            System.err.println("Error creating ad: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create ad: " + e.getMessage(), e);
        }
    }

    /**
     * Повертає ВСІ оголошення з репозиторію, незалежно від їх стану.
     * Якщо потрібні тільки активні оголошення, використовуйте getAllActiveAds().
     * Для адмінських потреб, де потрібні всі оголошення, можна також використовувати getAllAdsForAdmin().
     */
    @Override
    public List<Ad> getAllAds() {
        List<Ad> ads = adRepository.findAll();
        System.out.println("getAllAds() initial count (returning ALL ads): " + ads.size() + " ads"); // DEBUG
        return ads; // ВИПРАВЛЕНО: Повертаємо всі оголошення, фільтр за станом видалено.
    }

    @Override
    public Optional<Ad> getAdById(String adId) {
        if (adId == null || adId.trim().isEmpty()) {
            throw new InvalidInputException("Ad ID cannot be empty.");
        }
        return adRepository.findById(adId.trim());
    }

    @Override
    public List<Ad> getAdsByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new InvalidInputException("User ID cannot be empty.");
        }
        return adRepository.findBySellerId(userId.trim());
    }

    @Override
    public List<Ad> getAdsByCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new InvalidInputException("Category ID cannot be empty.");
        }
        return adRepository.findByCategoryId(categoryId.trim());
    }

    /**
     * Шукає оголошення за заданими критеріями.
     * ВАЖЛИВО: Цей метод фільтрує оголошення за станом ActiveAdState *перед* тим,
     * як передати їх стратегії пошуку. Якщо в базі даних немає оголошень
     * у стані ActiveAdState, або цей стан не встановлюється/зберігається коректно,
     * то MainController отримає 0 оголошень.
     * Перевірте ваші дані та логіку збереження стану оголошень.
     */

    // src/main/java/com/example/olx/application/service/impl/AdServiceImpl.java

    @Override
    public List<Ad> searchAds(String keyword, Double minPrice, Double maxPrice, String categoryId) {
        // --- ПОЧАТОК ВИПРАВЛЕННЯ ---

        // 1. Використовуємо HashMap, щоб гнучко керувати критеріями.
        Map<String, Object> criteria = new HashMap<>();

        // Додаємо критерії, які не потребують спеціальної обробки.
        // Стратегія сама впорається з null значеннями.
        criteria.put("keyword", keyword);
        criteria.put("minPrice", minPrice);
        criteria.put("maxPrice", maxPrice);

        // 2. Додаємо ключову логіку: якщо categoryId не є "All" (і не порожній),
        // тільки тоді додаємо його до критеріїв.
        if (categoryId != null && !categoryId.trim().isEmpty() && !categoryId.equalsIgnoreCase("All")) {
            criteria.put("categoryId", categoryId.trim());
        }
        // Якщо умова не виконується (categoryId is "All", null, or empty),
        // критерій "categoryId" не буде додано до мапи.

        // --- КІНЕЦЬ ВИПРАВЛЕННЯ ---

        // Пошук серед всіх оголошень, потім фільтрація за активним станом перед передачею стратегії
        List<Ad> allAdsFromRepo = adRepository.findAll();
        System.out.println("searchAds() initial count from repo: " + allAdsFromRepo.size() + " ads"); // DEBUG

        List<Ad> adsToSearch = allAdsFromRepo.stream()
                .filter(ad -> ad.getCurrentState() instanceof ActiveAdState) // Фільтруємо тільки активні
                .collect(Collectors.toList());

        System.out.println("searchAds() count after filtering for active (instanceof ActiveAdState): " + adsToSearch.size()); // DEBUG

        // Тепер передаємо відфільтровані (активні) оголошення до стратегії пошуку з правильними критеріями
        List<Ad> result = searchStrategy.search(adsToSearch, criteria);
        System.out.println("searchAds() count after searchStrategy: " + result.size()); //DEBUG
        return result;
    }

    @Override
    public void deleteAd(String adId, String currentUserId) throws UserNotFoundException {
        if (adId == null || adId.trim().isEmpty() || currentUserId == null || currentUserId.trim().isEmpty()) {
            throw new InvalidInputException("Ad ID and current User ID are required.");
        }

        Ad adToDelete = adRepository.findById(adId.trim())
                .orElseThrow(() -> new AdNotFoundException("Ad with ID " + adId + " not found."));
        User currentUser = userRepository.findById(currentUserId.trim())
                .orElseThrow(() -> new UserNotFoundException("User with ID " + currentUserId + " not found."));
        if (!adToDelete.getSellerId().equals(currentUserId.trim()) && currentUser.getUserType() != UserType.ADMIN) {
            throw new UnauthorizedActionException("User is not authorized to delete this ad.");
        }

        adRepository.deleteById(adId.trim());

        try {
            if (notificationService != null) {
                notificationService.sendSystemMessage("Ad '" + adToDelete.getTitle() + "' was deleted.");
            }
        } catch (Exception e) {
            System.err.println("Error sending deletion notification: " + e.getMessage());
        }
    }

    @Override
    public Ad updateAd(String adId, AdCreationRequest request, String currentUserId) throws UserNotFoundException {
        if (adId == null || adId.trim().isEmpty() || currentUserId == null || currentUserId.trim().isEmpty()) {
            throw new InvalidInputException("Ad ID and current User ID are required.");
        }

        if (request == null) {
            throw new InvalidInputException("Ad update data cannot be null.");
        }

        Ad existingAd = adRepository.findById(adId.trim())
                .orElseThrow(() -> new AdNotFoundException("Ad with ID " + adId + " not found."));
        User currentUser = userRepository.findById(currentUserId.trim())
                .orElseThrow(() -> new UserNotFoundException("User with ID " + currentUserId + " not found."));
        if (!existingAd.getSellerId().equals(currentUserId.trim()) && currentUser.getUserType() != UserType.ADMIN) {
            throw new UnauthorizedActionException("User is not authorized to update this ad.");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty() ||
                request.getPrice() < 0 || request.getCategoryId() == null || request.getCategoryId().trim().isEmpty()) {
            throw new InvalidInputException("Title, non-negative price, and category ID are required for updating an ad.");
        }

        categoryRepository.findById(request.getCategoryId().trim())
                .orElseThrow(() -> new InvalidInputException("Category with ID " + request.getCategoryId() + " not found during update."));
        // Оновлюємо поля існуючого оголошення
        existingAd.setTitle(request.getTitle().trim());
        existingAd.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        existingAd.setPrice(request.getPrice());
        existingAd.setCategoryId(request.getCategoryId().trim());
        existingAd.setImagePaths(request.getImagePaths());
        // Consider if the state should be updatable here, e.g., re-activating an ad.
        // For now, it preserves the existing state unless explicitly changed.

        return adRepository.save(existingAd);
    }

    @Override
    public List<Ad> getAllAdsForAdmin() {
        // Для адміна повертаємо всі оголошення без фільтрації
        return adRepository.findAll();
    }

    @Override
    public List<Ad> getAdsByState(AdState state) {
        if (state == null) {
            throw new InvalidInputException("Ad state cannot be null.");
        }

        return adRepository.findAll().stream()
                .filter(ad -> ad.getCurrentState().getClass().equals(state.getClass())) // This is a good way to check state by type
                .collect(Collectors.toList());
        // Using .collect(Collectors.toList())
    }
}
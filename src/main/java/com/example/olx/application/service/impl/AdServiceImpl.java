// src/main/java/com/example/olx/application/service/impl/AdServiceImpl.java
package com.example.olx.application.service.impl;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.application.service.port.NotificationServicePort;
import com.example.olx.application.service.strategy.AdSearchStrategy;
import com.example.olx.domain.exception.AdNotFoundException;
import com.example.olx.domain.exception.InvalidInputException;
import com.example.olx.domain.exception.UnauthorizedActionException;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.AdState;
import com.example.olx.domain.model.ActiveAdState;
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

public class AdServiceImpl implements AdServicePort {
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationServicePort notificationService;
    private final AdSearchStrategy searchStrategy;

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
        if (adId == null || adId.trim().isEmpty()) {
            throw new InvalidInputException("Ad ID cannot be null or empty");
        }
        if (newState == null) {
            throw new InvalidInputException("AdState cannot be null");
        }

        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new AdNotFoundException("Ad with ID " + adId + " not found"));
        ad.setCurrentState(newState);
        adRepository.save(ad);

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
        System.out.println("getAllActiveAds() initial count: " + ads.size() + " ads");

        List<Ad> activeAds = ads.stream()
                .filter(ad -> ad.getCurrentState() instanceof ActiveAdState)
                .collect(Collectors.toList());

        System.out.println("Active ads after filtering by instanceof ActiveAdState: " + activeAds.size());
        return activeAds;
    }

    /**
     * Новий метод для отримання активних оголошень з фільтрацією за типом
     * @param filterType "All", "Premium", "Urgent"
     * @return відфільтрований список активних оголошень
     */
    @Override
    public List<Ad> getFilteredActiveAds(String filterType) {
        List<Ad> activeAds = getAllActiveAds();

        if (filterType == null || filterType.equals("All") || filterType.equals("Всі")) {
            return activeAds;
        }

        return activeAds.stream()
                .filter(ad -> {
                    switch (filterType) {
                        case "Premium":
                        case "Тільки преміум":
                            return ad.isPremium();
                        case "Urgent":
                        case "Тільки терміново":
                            return ad.isUrgent();
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Ad createAd(AdCreationRequest request) throws UserNotFoundException {
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

        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new UserNotFoundException("Продавець з ідентифікатором " + request.getSellerId() + " не знайдений."));

        categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new InvalidInputException("Категорія з ідентифікатором " + request.getCategoryId() + " не знайдена."));

        try {
            Ad newAd = new Ad(
                    request.getTitle().trim(),
                    request.getDescription() != null ? request.getDescription().trim() : "",
                    request.getPrice(),
                    request.getCategoryId().trim(),
                    request.getSellerId().trim(),
                    request.getImagePaths()
            );

            newAd.setCurrentState(new ActiveAdState());

            // Встановлюємо додаткові властивості, якщо вони передані в запиті
            if (request.isPremium()) {
                newAd.setPremium(true);
            }
            if (request.isUrgent()) {
                newAd.setUrgent(true);
            }

            System.out.println("Creating ad: " + newAd.getTitle() + " with state: " + newAd.getCurrentState().getClass().getSimpleName());

            Ad savedAd = adRepository.save(newAd);

            if (savedAd == null) {
                throw new RuntimeException("Error saving ad to repository.");
            }

            System.out.println("Ad saved with ID: " + savedAd.getId());
            System.out.println("Ad premium: " + savedAd.isPremium() + ", urgent: " + savedAd.isUrgent());

            try {
                if (notificationService != null) {
                    notificationService.notifyUsersAboutNewAd(savedAd, userRepository.findAll());
                }
            } catch (Exception e) {
                System.err.println("Error sending new ad notifications: " + e.getMessage());
            }

            return savedAd;
        } catch (Exception e) {
            System.err.println("Error creating ad: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create ad: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Ad> getAllAds() {
        List<Ad> ads = adRepository.findAll();
        System.out.println("getAllAds() initial count (returning ALL ads): " + ads.size() + " ads");
        return ads;
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

    @Override
    public List<Ad> searchAds(String keyword, Double minPrice, Double maxPrice, String categoryId) {
        Map<String, Object> criteria = new HashMap<>();

        criteria.put("keyword", keyword);
        criteria.put("minPrice", minPrice);
        criteria.put("maxPrice", maxPrice);

        if (categoryId != null && !categoryId.trim().isEmpty() && !categoryId.equalsIgnoreCase("All")) {
            criteria.put("categoryId", categoryId.trim());
        }

        List<Ad> allAdsFromRepo = adRepository.findAll();
        System.out.println("searchAds() initial count from repo: " + allAdsFromRepo.size() + " ads");

        List<Ad> adsToSearch = allAdsFromRepo.stream()
                .filter(ad -> ad.getCurrentState() instanceof ActiveAdState)
                .collect(Collectors.toList());

        System.out.println("searchAds() count after filtering for active: " + adsToSearch.size());

        List<Ad> result = searchStrategy.search(adsToSearch, criteria);
        System.out.println("searchAds() count after searchStrategy: " + result.size());
        return result;
    }

    /**
     * Новий метод для пошуку з фільтрацією за типом оголошення
     */
    public List<Ad> searchAdsWithFilter(String keyword, Double minPrice, Double maxPrice, String categoryId, String filterType) {
        // Спочатку виконуємо звичайний пошук
        List<Ad> searchResults = searchAds(keyword, minPrice, maxPrice, categoryId);

        // Потім застосовуємо фільтр за типом
        if (filterType == null || filterType.equals("All") || filterType.equals("Всі")) {
            return searchResults;
        }

        return searchResults.stream()
                .filter(ad -> {
                    switch (filterType) {
                        case "Premium":
                        case "Тільки преміум":
                            return ad.isPremium();
                        case "Urgent":
                        case "Тільки терміново":
                            return ad.isUrgent();
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
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

        existingAd.setTitle(request.getTitle().trim());
        existingAd.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        existingAd.setPrice(request.getPrice());
        existingAd.setCategoryId(request.getCategoryId().trim());
        existingAd.setImagePaths(request.getImagePaths());

        // Оновлюємо премум та терміново статуси
        existingAd.setPremium(request.isPremium());
        existingAd.setUrgent(request.isUrgent());

        return adRepository.save(existingAd);
    }

    @Override
    public List<Ad> getAllAdsForAdmin() {
        return adRepository.findAll();
    }

    @Override
    public List<Ad> getAdsByState(AdState state) {
        if (state == null) {
            throw new InvalidInputException("Ad state cannot be null.");
        }

        return adRepository.findAll().stream()
                .filter(ad -> ad.getCurrentState().getClass().equals(state.getClass()))
                .collect(Collectors.toList());
    }
}
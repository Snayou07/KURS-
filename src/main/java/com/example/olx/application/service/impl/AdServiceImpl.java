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
import com.example.olx.domain.model.User;
import com.example.olx.domain.model.UserType;
import com.example.olx.domain.repository.AdRepository;
import com.example.olx.domain.repository.CategoryRepository;
import com.example.olx.domain.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

            // Зберігаємо оголошення в репозиторії
            Ad savedAd = adRepository.save(newAd);

            // Перевіряємо, що оголошення дійсно збережено
            if (savedAd == null) {
                throw new RuntimeException("Помилка при збереженні оголошення в репозиторії.");
            }

            // Відправляємо сповіщення користувачам про нове оголошення
            try {
                notificationService.notifyUsersAboutNewAd(savedAd, userRepository.findAll());
            } catch (Exception e) {
                // Логування помилки сповіщення, але не зупиняємо процес створення оголошення
                System.err.println("Помилка відправки сповіщень про нове оголошення: " + e.getMessage());
            }

            return savedAd;

        } catch (Exception e) {
            // Логування детальної інформації про помилку
            System.err.println("Помилка створення оголошення: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Не вдалося створити оголошення: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Ad> getAdById(String adId) {
        if (adId == null || adId.trim().isEmpty()) {
            throw new InvalidInputException("Ідентифікатор оголошення не може бути порожнім.");
        }
        return adRepository.findById(adId.trim());
    }

    @Override
    public List<Ad> getAllAds() {
        return adRepository.findAll();
    }

    @Override
    public List<Ad> getAdsByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new InvalidInputException("Ідентифікатор користувача не може бути порожнім.");
        }
        return adRepository.findBySellerId(userId.trim());
    }

    @Override
    public List<Ad> getAdsByCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new InvalidInputException("Ідентифікатор категорії не може бути порожнім.");
        }
        return adRepository.findByCategoryId(categoryId.trim());
    }

    @Override
    public List<Ad> searchAds(String keyword, Double minPrice, Double maxPrice, String categoryId) {
        Map<String, Object> criteria = Map.of(
                "keyword", keyword == null ? "" : keyword.trim(),
                "minPrice", minPrice == null ? Double.MIN_VALUE : minPrice,
                "maxPrice", maxPrice == null ? Double.MAX_VALUE : maxPrice,
                "categoryId", categoryId == null ? "" : categoryId.trim()
        );
        return searchStrategy.search(adRepository.findAll(), criteria);
    }

    @Override
    public void deleteAd(String adId, String currentUserId) throws UserNotFoundException {
        if (adId == null || adId.trim().isEmpty() || currentUserId == null || currentUserId.trim().isEmpty()) {
            throw new InvalidInputException("Ідентифікатор оголошення та поточного користувача є обов'язковими.");
        }

        Ad adToDelete = adRepository.findById(adId.trim())
                .orElseThrow(() -> new AdNotFoundException("Оголошення з ідентифікатором " + adId + " не знайдено."));

        User currentUser = userRepository.findById(currentUserId.trim())
                .orElseThrow(() -> new UserNotFoundException("Користувач з ідентифікатором " + currentUserId + " не знайдений."));

        if (!adToDelete.getSellerId().equals(currentUserId.trim()) && currentUser.getUserType() != UserType.ADMIN) {
            throw new UnauthorizedActionException("Користувач не має права видаляти це оголошення.");
        }

        adRepository.deleteById(adId.trim());

        try {
            notificationService.sendSystemMessage("Оголошення '" + adToDelete.getTitle() + "' було видалено.");
        } catch (Exception e) {
            System.err.println("Помилка відправки сповіщення про видалення: " + e.getMessage());
        }
    }

    @Override
    public Ad updateAd(String adId, AdCreationRequest request, String currentUserId) throws UserNotFoundException {
        if (adId == null || adId.trim().isEmpty() || currentUserId == null || currentUserId.trim().isEmpty()) {
            throw new InvalidInputException("Ідентифікатор оголошення та поточного користувача є обов'язковими.");
        }

        if (request == null) {
            throw new InvalidInputException("Дані для оновлення оголошення не можуть бути null.");
        }

        Ad existingAd = adRepository.findById(adId.trim())
                .orElseThrow(() -> new AdNotFoundException("Оголошення з ідентифікатором " + adId + " не знайдено."));

        User currentUser = userRepository.findById(currentUserId.trim())
                .orElseThrow(() -> new UserNotFoundException("Користувач з ідентифікатором " + currentUserId + " не знайдений."));

        if (!existingAd.getSellerId().equals(currentUserId.trim()) && currentUser.getUserType() != UserType.ADMIN) {
            throw new UnauthorizedActionException("Користувач не має права оновлювати це оголошення.");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty() ||
                request.getPrice() < 0 || request.getCategoryId() == null || request.getCategoryId().trim().isEmpty()) {
            throw new InvalidInputException("Назва, невід'ємна ціна та ідентифікатор категорії є обов'язковими для оновлення оголошення.");
        }

        categoryRepository.findById(request.getCategoryId().trim())
                .orElseThrow(() -> new InvalidInputException("Категорія з ідентифікатором " + request.getCategoryId() + " не знайдена під час оновлення."));

        // Оновлюємо поля існуючого оголошення
        existingAd.setTitle(request.getTitle().trim());
        existingAd.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        existingAd.setPrice(request.getPrice());
        existingAd.setCategoryId(request.getCategoryId().trim());
        existingAd.setImagePaths(request.getImagePaths());

        return adRepository.save(existingAd);
    }
}
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
    private final CategoryRepository categoryRepository; // Для валідації категорії
    private final NotificationServicePort notificationService; // Observer
    private final AdSearchStrategy searchStrategy; // Strategy

    public AdServiceImpl(AdRepository adRepository, UserRepository userRepository,
                         CategoryRepository categoryRepository,
                         NotificationServicePort notificationService,
                         AdSearchStrategy searchStrategy) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.notificationService = notificationService;
        this.searchStrategy = searchStrategy;
    }

    @Override
    public Ad createAd(AdCreationRequest request) throws UserNotFoundException {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty() ||
                request.getPrice() < 0 || request.getCategoryId() == null || request.getSellerId() == null) {
            throw new InvalidInputException("Назва, невід'ємна ціна, ідентифікатор категорії та ідентифікатор продавця є обов'язковими для оголошення.");
        }

        userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new UserNotFoundException("Продавець з ідентифікатором " + request.getSellerId() + " не знайдений."));

        categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new InvalidInputException("Категорія з ідентифікатором " + request.getCategoryId() + " не знайдена."));

        Ad newAd = new Ad(request.getTitle(), request.getDescription(), request.getPrice(),
                request.getCategoryId(), request.getSellerId(), request.getImagePaths()); // Додано imagePaths
        Ad savedAd = adRepository.save(newAd);

        notificationService.notifyUsersAboutNewAd(savedAd, userRepository.findAll());
        return savedAd;
    }

    @Override
    public Optional<Ad> getAdById(String adId) {
        if (adId == null || adId.trim().isEmpty()) {
            throw new InvalidInputException("Ідентифікатор оголошення не може бути порожнім.");
        }
        return adRepository.findById(adId);
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
        return adRepository.findBySellerId(userId);
    }

    @Override
    public List<Ad> getAdsByCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new InvalidInputException("Ідентифікатор категорії не може бути порожнім.");
        }
        // Тут можна додати логіку для отримання оголошень з усіх підкатегорій
        return adRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Ad> searchAds(String keyword, Double minPrice, Double maxPrice, String categoryId) {
        Map<String, Object> criteria = Map.of(
                "keyword", keyword == null ? "" : keyword, // Передаємо порожній рядок, якщо null
                "minPrice", minPrice == null ? Double.MIN_VALUE : minPrice,
                "maxPrice", maxPrice == null ? Double.MAX_VALUE : maxPrice,
                "categoryId", categoryId == null ? "" : categoryId
        );
        return searchStrategy.search(adRepository.findAll(), criteria);
    }

    @Override
    public void deleteAd(String adId, String currentUserId) throws UserNotFoundException {
        if (adId == null || currentUserId == null) {
            throw new InvalidInputException("Ідентифікатор оголошення та ідентифікатор поточного користувача є обов'язковими.");
        }
        Ad adToDelete = adRepository.findById(adId)
                .orElseThrow(() -> new AdNotFoundException("Оголошення з ідентифікатором " + adId + " не знайдене."));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Користувач з ідентифікатором " + currentUserId + " не знайдений."));

        if (!adToDelete.getSellerId().equals(currentUserId) && currentUser.getUserType() != UserType.ADMIN) {
            throw new UnauthorizedActionException("Користувач не має права видаляти це оголошення.");
        }
        adRepository.deleteById(adId);
        notificationService.sendSystemMessage("Оголошення '" + adToDelete.getTitle() + "' було видалено.");
    }

    @Override
    public Ad updateAd(String adId, AdCreationRequest request, String currentUserId) throws UserNotFoundException {
        if (adId == null || currentUserId == null) {
            throw new InvalidInputException("Ідентифікатор оголошення та ідентифікатор поточного користувача є обов'язковими.");
        }
        Ad existingAd = adRepository.findById(adId)
                .orElseThrow(() -> new AdNotFoundException("Оголошення з ідентифікатором " + adId + " не знайдене."));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Користувач з ідентифікатором " + currentUserId + " не знайдений."));

        if (!existingAd.getSellerId().equals(currentUserId) && currentUser.getUserType() != UserType.ADMIN) {
            throw new UnauthorizedActionException("Користувач не має права оновлювати це оголошення.");
        }

        // Валідація даних з request
        if (request.getTitle() == null || request.getTitle().trim().isEmpty() ||
                request.getPrice() < 0 || request.getCategoryId() == null) {
            throw new InvalidInputException("Назва, невід'ємна ціна та ідентифікатор категорії є обов'язковими для оновлення оголошення.");
        }
        categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new InvalidInputException("Категорія з ідентифікатором " + request.getCategoryId() + " не знайдена під час оновлення."));

        // Оновлюємо поля
        existingAd.setTitle(request.getTitle());
        existingAd.setDescription(request.getDescription());
        existingAd.setPrice(request.getPrice());
        existingAd.setCategoryId(request.getCategoryId());
        existingAd.setImagePaths(request.getImagePaths()); // Оновлюємо шляхи до фото

        return adRepository.save(existingAd); // Метод save репозиторію має обробляти оновлення
    }
}
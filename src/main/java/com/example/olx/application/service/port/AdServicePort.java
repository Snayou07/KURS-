// src/main/java/com/example/olx/application/service/port/AdServicePort.java
package com.example.olx.application.service.port;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.AdState;

import java.util.List;
import java.util.Optional;

public interface AdServicePort {

    // Основні методи для роботи з оголошеннями
    Ad createAd(AdCreationRequest request) throws UserNotFoundException;

    List<Ad> getAllAds();
    List<Ad> getAllActiveAds();

    /**
     * Новий метод для отримання активних оголошень з фільтрацією
     * @param filterType "All", "Premium", "Urgent" або українські еквіваленти
     * @return відфільтрований список активних оголошень
     */
    List<Ad> getFilteredActiveAds(String filterType);

    Optional<Ad> getAdById(String adId);
    List<Ad> getAdsByUserId(String userId);
    List<Ad> getAdsByCategoryId(String categoryId);

    // Методи пошуку
    List<Ad> searchAds(String keyword, Double minPrice, Double maxPrice, String categoryId);

    /**
     * Пошук оголошень з додатковою фільтрацією за типом
     * @param keyword ключове слово
     * @param minPrice мінімальна ціна
     * @param maxPrice максимальна ціна
     * @param categoryId ID категорії
     * @param filterType тип фільтра ("All", "Premium", "Urgent")
     * @return відфільтрований список оголошень
     */
    List<Ad> searchAdsWithFilter(String keyword, Double minPrice, Double maxPrice, String categoryId, String filterType);

    // Методи управління оголошеннями
    Ad updateAd(String adId, AdCreationRequest request, String currentUserId) throws UserNotFoundException;
    void deleteAd(String adId, String currentUserId) throws UserNotFoundException;

    // Методи роботи зі станами
    void changeAdState(String adId, AdState newState) throws UserNotFoundException;
    List<Ad> getAdsByState(AdState state);

    // Адміністративні методи
    List<Ad> getAllAdsForAdmin();
}
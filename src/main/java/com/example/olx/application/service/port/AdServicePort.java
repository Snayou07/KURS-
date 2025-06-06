// com/example/olx/application/service/port/AdServicePort.java
package com.example.olx.application.service.port;

import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;
import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.domain.model.AdState;

import java.util.List;
import java.util.Optional;

public interface AdServicePort {
    void changeAdState(String adId, AdState newState) throws UserNotFoundException;
    Ad createAd(AdCreationRequest request) throws UserNotFoundException;
    Optional<Ad> getAdById(String adId);

    // Основной метод для главного меню - только активные объявления
    List<Ad> getAllAds();

    // Для админки - все объявления
    List<Ad> getAllAdsForAdmin();
    // У AdServicePort.java додайте:
    List<Ad> getAllActiveAds();
    // Активные объявления по состоянию
    List<Ad> getAdsByState(AdState state);

    List<Ad> getAdsByUserId(String userId);
    List<Ad> getAdsByCategoryId(String categoryId);
    List<Ad> searchAds(String keyword, Double minPrice, Double maxPrice, String categoryId);
    void deleteAd(String adId, String currentUserId) throws UserNotFoundException;
    Ad updateAd(String adId, AdCreationRequest request, String currentUserId) throws UserNotFoundException;
}
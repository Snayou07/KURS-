// com/example/olx/application/service/port/AdServicePort.java
package com.example.olx.application.service.port;

import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;
import com.example.olx.application.dto.AdCreationRequest; // DTO для створення
import java.util.List;
import java.util.Optional;

public interface AdServicePort {
    Ad createAd(AdCreationRequest request) throws UserNotFoundException;
    Optional<Ad> getAdById(String adId);
    List<Ad> getAllAds();
    List<Ad> getAdsByUserId(String userId);
    List<Ad> getAdsByCategoryId(String categoryId); // Пошук по категорії
    List<Ad> searchAds(String keyword, Double minPrice, Double maxPrice, String categoryId); // Для Strategy
    void deleteAd(String adId, String currentUserId) throws UserNotFoundException; // Поточний користувач для перевірки прав
    Ad updateAd(String adId, AdCreationRequest request, String currentUserId) throws UserNotFoundException; // Оновлення оголошення
}
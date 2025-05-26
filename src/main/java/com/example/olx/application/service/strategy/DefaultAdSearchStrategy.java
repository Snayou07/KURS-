// com/example/olx/application/service/strategy/DefaultAdSearchStrategy.java
package com.example.olx.application.service.strategy;

import com.example.olx.domain.model.Ad;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultAdSearchStrategy implements AdSearchStrategy {
    @Override
    public List<Ad> search(List<Ad> allAds, Map<String, Object> criteria) {
        String keyword = (String) criteria.getOrDefault("keyword", null);
        Double minPrice = (Double) criteria.getOrDefault("minPrice", null);
        Double maxPrice = (Double) criteria.getOrDefault("maxPrice", null);
        String categoryId = (String) criteria.getOrDefault("categoryId", null);

        return allAds.stream()
                .filter(ad -> keyword == null || ad.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        ad.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .filter(ad -> minPrice == null || ad.getPrice() >= minPrice)
                .filter(ad -> maxPrice == null || ad.getPrice() <= maxPrice)
                .filter(ad -> categoryId == null || ad.getCategoryId().equals(categoryId)) // Потрібна рекурсія для категорій
                .collect(Collectors.toList());
    }
}
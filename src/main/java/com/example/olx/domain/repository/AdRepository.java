
// com/example/olx/domain/repository/AdRepository.java
package com.example.olx.domain.repository;

import com.example.olx.domain.model.Ad;
import java.util.List;
import java.util.Optional;

public interface AdRepository {
    Ad save(Ad ad);
    Optional<Ad> findById(String id);
    List<Ad> findAll();
    List<Ad> findBySellerId(String sellerId);
    List<Ad> findByCategoryId(String categoryId);
    void deleteById(String id);
}


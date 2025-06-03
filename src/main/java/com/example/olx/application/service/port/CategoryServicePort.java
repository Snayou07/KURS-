package com.example.olx.application.service.port;

import com.example.olx.domain.model.CategoryComponent;
import java.util.List;
import java.util.Optional;

public interface CategoryServicePort {
    void initializeCategories(List<CategoryComponent> rootCategories);
    List<CategoryComponent> getAllRootCategories();
    Optional<CategoryComponent> findCategoryById(String id);

    <T> Optional<T> getCategoryById(String categoryId);
}
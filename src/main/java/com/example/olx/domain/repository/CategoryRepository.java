package com.example.olx.domain.repository;

import com.example.olx.domain.model.CategoryComponent;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    CategoryComponent save(CategoryComponent category); // Може бути складніше для дерева
    Optional<CategoryComponent> findById(String id);
    List<CategoryComponent> findAllRoots(); // Отримати кореневі категорії
    void setAllCategories(List<CategoryComponent> categories); // Для завантаження всієї структури
}
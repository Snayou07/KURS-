
// CategoryRepository.java - ВІДСУТНІЙ ІНТЕРФЕЙС
package com.example.olx.domain.repository;

import com.example.olx.domain.model.CategoryComponent;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    CategoryComponent save(CategoryComponent category);
    void setAllCategories(List<CategoryComponent> categories);
    Optional<CategoryComponent> findById(String id);
    List<CategoryComponent> findAllRoots();
    void deleteById(String id);
}
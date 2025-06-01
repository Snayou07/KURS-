// com/example/olx/application/service/impl/CategoryServiceImpl.java
package com.example.olx.application.service.impl;

import com.example.olx.application.service.port.CategoryServicePort;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.exception.InvalidInputException;
import com.example.olx.domain.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryServiceImpl implements CategoryServicePort {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void initializeCategories(List<CategoryComponent> rootCategories) {
        if (rootCategories == null) {
            throw new InvalidInputException("Cannot initialize with null list of categories.");
        }
        if (rootCategories.isEmpty()) {
            throw new InvalidInputException("Cannot initialize with empty list of categories.");
        }

        // Перевіряємо, чи всі елементи в списку не null
        for (CategoryComponent category : rootCategories) {
            if (category == null) {
                throw new InvalidInputException("Cannot initialize categories: one or more category elements are null.");
            }
        }

        categoryRepository.setAllCategories(rootCategories);
    }

    @Override
    public List<CategoryComponent> getAllRootCategories() {
        List<CategoryComponent> categories = categoryRepository.findAllRoots();
        // Повертаємо порожній список замість null для безпеки
        if (categories == null) {
            return new ArrayList<>();
        }

        // Фільтруємо null елементи, якщо вони є
        List<CategoryComponent> filteredCategories = new ArrayList<>();
        for (CategoryComponent category : categories) {
            if (category != null) {
                filteredCategories.add(category);
            }
        }

        return filteredCategories;
    }

    @Override
    public Optional<CategoryComponent> findCategoryById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidInputException("Category ID cannot be empty.");
        }

        Optional<CategoryComponent> result = categoryRepository.findById(id);

        // Перевіряємо, чи результат не містить null
        if (result != null && result.isPresent() && result.get() == null) {
            return Optional.empty();
        }

        return result != null ? result : Optional.empty();
    }
}
// com/example/olx/application/service/impl/CategoryServiceImpl.java
package com.example.olx.application.service.impl;

import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.exception.InvalidInputException;
import com.example.olx.domain.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

public class CategoryServiceImpl {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void initializeCategories(List<CategoryComponent> rootCategories) {
        if (rootCategories == null || rootCategories.isEmpty()) {
            throw new InvalidInputException("Cannot initialize with empty list of categories.");
        }
        categoryRepository.setAllCategories(rootCategories);
    }

    @Override
    public List<CategoryComponent> getAllRootCategories() {
        return categoryRepository.findAllRoots();
    }

    @Override
    public Optional<CategoryComponent> findCategoryById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidInputException("Category ID cannot be empty.");
        }
        return categoryRepository.findById(id);
    }
}
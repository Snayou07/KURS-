// src/main/java/com/example/olx/application/service/impl/CategoryServiceImpl.java
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

        try {
            categoryRepository.setAllCategories(rootCategories);
            System.out.println("Категорії успішно ініціалізовано. Кількість кореневих категорій: " + rootCategories.size());

            // Логування для дебагу
            for (CategoryComponent category : rootCategories) {
                System.out.println("Ініціалізована категорія: " + category.getName() + " (ID: " + category.getId() + ")");
            }
        } catch (Exception e) {
            System.err.println("Помилка ініціалізації категорій: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Не вдалося ініціалізувати категорії", e);
        }
    }

    @Override
    public List<CategoryComponent> getAllRootCategories() {
        try {
            List<CategoryComponent> categories = categoryRepository.findAllRoots();

            // Повертаємо порожній список замість null для безпеки
            if (categories == null) {
                System.out.println("Категорії не знайдено в репозиторії, повертаємо порожній список");
                return new ArrayList<>();
            }

            // Фільтруємо null елементи, якщо вони є
            List<CategoryComponent> filteredCategories = new ArrayList<>();
            for (CategoryComponent category : categories) {
                if (category != null) {
                    filteredCategories.add(category);
                } else {
                    System.err.println("УВАГА: Знайдено null категорію в списку, пропускаємо її");
                }
            }

            System.out.println("Повернуто кореневих категорій: " + filteredCategories.size());

            // Додатковий дебаг
            for (CategoryComponent category : filteredCategories) {
                System.out.println("Категорія: " + category.getName() + " (ID: " + category.getId() + ")");
            }

            return filteredCategories;

        } catch (Exception e) {
            System.err.println("Помилка отримання кореневих категорій: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Повертаємо порожній список в разі помилки
        }
    }

    @Override
    public Optional<CategoryComponent> findCategoryById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidInputException("Category ID cannot be empty.");
        }

        try {
            Optional<CategoryComponent> result = categoryRepository.findById(id.trim());

            // Перевіряємо, чи результат не містить null
            if (result != null && result.isPresent() && result.get() == null) {
                System.err.println("УВАГА: Знайдено null категорію для ID: " + id);
                return Optional.empty();
            }

            if (result != null && result.isPresent()) {
                System.out.println("Знайдено категорію: " + result.get().getName() + " для ID: " + id);
            } else {
                System.out.println("Категорію з ID " + id + " не знайдено");
            }

            return result != null ? result : Optional.empty();

        } catch (Exception e) {
            System.err.println("Помилка пошуку категорії з ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> getCategoryById(String categoryId) {
        return Optional.empty();
    }
}
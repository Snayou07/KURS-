// com/example/olx/infrastructure/persistence/FileCategoryRepositoryImpl.java
package com.example.olx.infrastructure.persistence;

import com.example.olx.domain.model.Category;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileCategoryRepositoryImpl implements CategoryRepository {
    private final SessionManager sessionManager;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FileCategoryRepositoryImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public CategoryComponent save(CategoryComponent category) {
        if (category == null) {
            throw new IllegalArgumentException("Категорія не може бути null");
        }

        lock.writeLock().lock();
        try {
            // Це дуже спрощено. Потрібна логіка для оновлення існуючої категорії в дереві
            // або додавання нової до відповідного батька.
            // Зараз просто замінюємо, якщо ID співпадає, або додаємо до кореня.
            List<CategoryComponent> roots = sessionManager.getCategoriesFromState();

            boolean isUpdate = roots.stream().anyMatch(c -> c.getId().equals(category.getId()));

            if (isUpdate) {
                roots.removeIf(c -> c.getId().equals(category.getId()));
                System.out.println("Оновлено існуючу категорію: " + category.getId());
            } else {
                System.out.println("Додано нову категорію: " + category.getId());
            }

            roots.add(category); // Це не зовсім коректно для структури дерева.
            sessionManager.setCategoriesInState(roots); // Оновлюємо весь список категорій

            // ВАЖЛИВО: Зберігаємо стан після кожної зміни
            try {
                sessionManager.saveState();
                System.out.println("Стан програми збережено після операції з категорією");
            } catch (Exception e) {
                System.err.println("ПОМИЛКА: Не вдалося зберегти стан після створення/оновлення категорії: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Помилка збереження категорії", e);
            }

            return category;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Цей метод більш підходить для ініціалізації/повного оновлення категорій
    @Override
    public void setAllCategories(List<CategoryComponent> categories) {
        if (categories == null) {
            throw new IllegalArgumentException("Список категорій не може бути null");
        }

        lock.writeLock().lock();
        try {
            sessionManager.setCategoriesInState(categories);

            // Зберігаємо стан після встановлення всіх категорій
            try {
                sessionManager.saveState();
                System.out.println("Всі категорії встановлені та стан збережено. Кількість: " + categories.size());
            } catch (Exception e) {
                System.err.println("ПОМИЛКА: Не вдалося зберегти стан після встановлення всіх категорій: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Помилка збереження категорій", e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<CategoryComponent> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        lock.readLock().lock();
        try {
            return findByIdRecursive(sessionManager.getCategoriesFromState(), id.trim());
        } finally {
            lock.readLock().unlock();
        }
    }

    private Optional<CategoryComponent> findByIdRecursive(List<CategoryComponent> categories, String id) {
        for (CategoryComponent component : categories) {
            if (component.getId().equals(id)) {
                return Optional.of(component);
            }
            if (component instanceof Category) {
                Optional<CategoryComponent> foundInChildren = findByIdRecursive(((Category) component).getSubCategories(), id);
                if (foundInChildren.isPresent()) {
                    return foundInChildren;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<CategoryComponent> findAllRoots() {
        lock.readLock().lock();
        try {
            List<CategoryComponent> categories = sessionManager.getCategoriesFromState();
            System.out.println("Знайдено кореневих категорій: " + categories.size());
            return categories;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return; // Нічого не робимо для порожнього ID
        }

        lock.writeLock().lock();
        try {
            List<CategoryComponent> roots = sessionManager.getCategoriesFromState();
            boolean removed = roots.removeIf(c -> c.getId().equals(id.trim()));

            // Також потрібно видалити з дочірніх категорій рекурсивно
            removeFromChildrenRecursive(roots, id.trim());

            sessionManager.setCategoriesInState(roots);

            if (removed) {
                try {
                    sessionManager.saveState();
                    System.out.println("Категорію " + id + " видалено та стан збережено");
                } catch (Exception e) {
                    System.err.println("Помилка збереження стану після видалення категорії: " + e.getMessage());
                    throw new RuntimeException("Помилка видалення категорії", e);
                }
            } else {
                System.out.println("Категорію з ID " + id + " не знайдено для видалення");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeFromChildrenRecursive(List<CategoryComponent> categories, String id) {
        for (CategoryComponent component : categories) {
            if (component instanceof Category) {
                Category category = (Category) component;
                category.getSubCategories().removeIf(c -> c.getId().equals(id));
                removeFromChildrenRecursive(category.getSubCategories(), id);
            }
        }
    }
}
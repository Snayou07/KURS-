// com/example/olx/infrastructure/persistence/FileCategoryRepositoryImpl.java
package com.example.olx.infrastructure.persistence;

import com.example.olx.domain.model.Category;
import com.example.olx.domain.model.CategoryComponent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileCategoryRepositoryImpl implements CategoryRepository {
    private final SessionManager sessionManager;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FileCategoryRepositoryImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // Збереження окремої категорії може бути складним для дерева.
    // Зазвичай або зберігається все дерево, або операції йдуть через батьківські вузли.
    // Для простоти, цей метод може бути не дуже корисним без контексту дерева.
    @Override
    public CategoryComponent save(CategoryComponent category) {
        lock.writeLock().lock();
        try {
            // Це дуже спрощено. Потрібна логіка для оновлення існуючої категорії в дереві
            // або додавання нової до відповідного батька.
            // Зараз просто замінюємо, якщо ID співпадає, або додаємо до кореня.
            List<CategoryComponent> roots = sessionManager.getCategoriesFromState();
            roots.removeIf(c -> c.getId().equals(category.getId()));
            roots.add(category); // Це не зовсім коректно для структури дерева.
            sessionManager.setCategoriesInState(roots); // Оновлюємо весь список категорій
            return category;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Цей метод більш підходить для ініціалізації/повного оновлення категорій
    @Override
    public void setAllCategories(List<CategoryComponent> categories) {
        lock.writeLock().lock();
        try {
            sessionManager.setCategoriesInState(categories);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<CategoryComponent> findById(String id) {
        lock.readLock().lock();
        try {
            return findByIdRecursive(sessionManager.getCategoriesFromState(), id);
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
            return sessionManager.getCategoriesFromState();
        } finally {
            lock.readLock().unlock();
        }
    }
}
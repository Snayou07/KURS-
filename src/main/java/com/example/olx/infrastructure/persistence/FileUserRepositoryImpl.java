// src/main/java/com/example/olx/infrastructure/persistence/FileUserRepositoryImpl.java
package com.example.olx.infrastructure.persistence;

import com.example.olx.domain.model.User;
import com.example.olx.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileUserRepositoryImpl implements UserRepository {
    private final SessionManager sessionManager;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FileUserRepositoryImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Користувач не може бути null");
        }

        lock.writeLock().lock();
        try {
            // Перевіряємо, чи користувач вже існує (оновлення) або це новий користувач
            boolean isUpdate = sessionManager.getUsersFromState().stream()
                    .anyMatch(u -> u.getUserId().equals(user.getUserId()));

            if (isUpdate) {
                sessionManager.updateUserInState(user);
                System.out.println("Оновлено існуючого користувача: " + user.getUsername());
            } else {
                sessionManager.addUserToState(user);
                System.out.println("Додано нового користувача: " + user.getUsername());
            }

            // ВАЖЛИВО: Зберігаємо стан після кожної зміни
            try {
                sessionManager.saveState();
                System.out.println("Стан програми збережено після операції з користувачем");
            } catch (Exception e) {
                System.err.println("ПОМИЛКА: Не вдалося зберегти стан після створення/оновлення користувача: " + e.getMessage());
                e.printStackTrace();
                // Відкатуємо зміни при помилці збереження
                if (isUpdate) {
                    // При оновленні складно відкатити, тому просто логуємо
                    System.err.println("Неможливо відкатити оновлення користувача");
                } else {
                    // При додаванні видаляємо користувача зі стану
                    sessionManager.removeUserFromState(user.getUserId());
                    System.out.println("Відкатано додавання користувача через помилку збереження");
                }
                throw new RuntimeException("Помилка збереження користувача", e);
            }

            return user;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        lock.readLock().lock();
        try {
            return sessionManager.getUsersFromState().stream()
                    .filter(user -> user.getUserId().equals(id.trim()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }

        lock.readLock().lock();
        try {
            return sessionManager.getUsersFromState().stream()
                    .filter(user -> user.getEmail().equals(email.trim()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        lock.readLock().lock();
        try {
            return sessionManager.getUsersFromState().stream()
                    .filter(user -> user.getUsername().equals(username.trim()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<User> findAll() {
        lock.readLock().lock();
        try {
            List<User> users = sessionManager.getUsersFromState();
            System.out.println("Знайдено користувачів: " + users.size());
            return users;
        } finally {
            lock.readLock().unlock();
        }
    }

    // Додаємо метод для видалення користувача, якщо потрібен
    public void deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return; // Нічого не робимо для порожнього ID
        }

        lock.writeLock().lock();
        try {
            boolean removed = sessionManager.getUsersFromState().removeIf(user -> user.getUserId().equals(id.trim()));

            if (removed) {
                try {
                    sessionManager.saveState();
                    System.out.println("Користувача " + id + " видалено та стан збережено");
                } catch (Exception e) {
                    System.err.println("Помилка збереження стану після видалення користувача: " + e.getMessage());
                    throw new RuntimeException("Помилка видалення користувача", e);
                }
            } else {
                System.out.println("Користувача з ID " + id + " не знайдено для видалення");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
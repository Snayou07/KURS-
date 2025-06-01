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
        lock.writeLock().lock();
        try {
            // Перевіряємо, чи користувач вже існує (оновлення) або це новий користувач
            boolean isUpdate = sessionManager.getUsersFromState().stream()
                    .anyMatch(u -> u.getUserId().equals(user.getUserId()));

            if (isUpdate) {
                sessionManager.updateUserInState(user);
            } else {
                sessionManager.addUserToState(user);
            }

            // ВАЖЛИВО: Зберігаємо стан після кожної зміни
            try {
                sessionManager.saveState();
            } catch (Exception e) {
                System.err.println("Помилка збереження стану користувача: " + e.getMessage());
                // Можна додати логування або обробку помилки
            }

            return user;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<User> findById(String id) {
        lock.readLock().lock();
        try {
            return sessionManager.getUsersFromState().stream()
                    .filter(user -> user.getUserId().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        lock.readLock().lock();
        try {
            return sessionManager.getUsersFromState().stream()
                    .filter(user -> user.getEmail().equals(email))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        lock.readLock().lock();
        try {
            return sessionManager.getUsersFromState().stream()
                    .filter(user -> user.getUsername().equals(username))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<User> findAll() {
        lock.readLock().lock();
        try {
            return sessionManager.getUsersFromState();
        } finally {
            lock.readLock().unlock();
        }
    }
}
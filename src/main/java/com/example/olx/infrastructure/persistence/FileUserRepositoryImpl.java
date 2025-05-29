// com/example/olx/infrastructure/persistence/FileUserRepositoryImpl.java
package com.example.olx.infrastructure.persistence;

import com.example.olx.domain.model.User;
import com.example.olx.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock; // Для кращої синхронізації

public class FileUserRepositoryImpl implements UserRepository {
    private final SessionManager sessionManager;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(); // Для потокобезпечності

    public FileUserRepositoryImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public User save(User user) {
        lock.writeLock().lock();
        try {
            boolean isUpdate = sessionManager.getUsersFromState().stream()
                    .anyMatch(u -> u.getUserId().equals(user.getUserId()));
            if (isUpdate) {
                sessionManager.updateUserInState(user);
            } else {
                // Перевірка на дублікат username для нових користувачів
                if (sessionManager.getUsersFromState().stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
                    // Цей виняток краще кидати на рівні сервісу, але для прикладу
                    // throw new DuplicateUserException("Username " + user.getUsername() + " already exists in repository.");
                }
                sessionManager.addUserToState(user);
            }
            // sessionManager.saveState(); // Збереження стану викликається окремо, на вищому рівні
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
            return sessionManager.getUsersFromState(); // Повертає копію з SessionManager
        } finally {
            lock.readLock().unlock();
        }
    }
    /*
    @Override
    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            sessionManager.removeUserFromState(id);
            // sessionManager.saveState();
        } finally {
            lock.writeLock().unlock();
        }
    }
    */

}
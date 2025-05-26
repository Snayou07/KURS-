// com/example/olx/infrastructure/persistence/FileAdRepositoryImpl.java
package com.example.olx.infrastructure.persistence;

import com.example.olx.domain.model.Ad;
import com.example.olx.domain.repository.AdRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileAdRepositoryImpl implements AdRepository {
    private final SessionManager sessionManager;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FileAdRepositoryImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Ad save(Ad ad) {
        lock.writeLock().lock();
        try {
            boolean isUpdate = sessionManager.getAdsFromState().stream()
                    .anyMatch(a -> a.getAdId().equals(ad.getAdId()));
            if (isUpdate) {
                sessionManager.updateAdInState(ad);
            } else {
                sessionManager.addAdToState(ad);
            }
            return ad;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Ad> findById(String id) {
        lock.readLock().lock();
        try {
            return sessionManager.getAdsFromState().stream()
                    .filter(ad -> ad.getAdId().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Ad> findAll() {
        lock.readLock().lock();
        try {
            return sessionManager.getAdsFromState();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Ad> findBySellerId(String sellerId) {
        lock.readLock().lock();
        try {
            return sessionManager.getAdsFromState().stream()
                    .filter(ad -> ad.getSellerId().equals(sellerId))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Ad> findByCategoryId(String categoryId) {
        lock.readLock().lock();
        try {
            // Потрібно реалізувати пошук по вкладених категоріях, якщо categoryId може бути батьківським.
            // Для простоти, зараз шукаємо точне співпадіння.
            return sessionManager.getAdsFromState().stream()
                    .filter(ad -> ad.getCategoryId().equals(categoryId))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            sessionManager.removeAdFromState(id);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
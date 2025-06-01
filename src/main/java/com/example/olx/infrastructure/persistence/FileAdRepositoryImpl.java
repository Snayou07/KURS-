// src/main/java/com/example/olx/infrastructure/persistence/FileAdRepositoryImpl.java
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
        if (ad == null) {
            throw new IllegalArgumentException("Оголошення не може бути null");
        }

        lock.writeLock().lock();
        try {
            boolean isUpdate = sessionManager.getAdsFromState().stream()
                    .anyMatch(a -> a.getAdId().equals(ad.getAdId()));

            if (isUpdate) {
                sessionManager.updateAdInState(ad);
                System.out.println("Оновлено існуюче оголошення: " + ad.getAdId());
            } else {
                sessionManager.addAdToState(ad);
                System.out.println("Додано нове оголошення: " + ad.getAdId());
            }

            // ВАЖЛИВО: Зберігаємо стан після кожної зміни
            try {
                sessionManager.saveState();
                System.out.println("Стан програми збережено після операції з оголошенням");
            } catch (Exception e) {
                System.err.println("ПОМИЛКА: Не вдалося зберегти стан після створення/оновлення оголошення: " + e.getMessage());
                e.printStackTrace();
                // Можна розглянути відкат змін або повторну спробу
                throw new RuntimeException("Помилка збереження оголошення", e);
            }

            return ad;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Ad> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        lock.readLock().lock();
        try {
            return sessionManager.getAdsFromState().stream()
                    .filter(ad -> ad.getAdId().equals(id.trim()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Ad> findAll() {
        lock.readLock().lock();
        try {
            List<Ad> ads = sessionManager.getAdsFromState();
            System.out.println("Знайдено оголошень: " + ads.size());
            return ads;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Ad> findBySellerId(String sellerId) {
        if (sellerId == null || sellerId.trim().isEmpty()) {
            return List.of(); // Повертаємо порожній список
        }

        lock.readLock().lock();
        try {
            return sessionManager.getAdsFromState().stream()
                    .filter(ad -> ad.getSellerId().equals(sellerId.trim()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Ad> findByCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return List.of(); // Повертаємо порожній список
        }

        lock.readLock().lock();
        try {
            return sessionManager.getAdsFromState().stream()
                    .filter(ad -> ad.getCategoryId().equals(categoryId.trim()))
                    .collect(Collectors.toList());
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
            boolean removed = sessionManager.getAdsFromState().removeIf(ad -> ad.getAdId().equals(id.trim()));

            if (removed) {
                try {
                    sessionManager.saveState();
                    System.out.println("Оголошення " + id + " видалено та стан збережено");
                } catch (Exception e) {
                    System.err.println("Помилка збереження стану після видалення оголошення: " + e.getMessage());
                    throw new RuntimeException("Помилка видалення оголошення", e);
                }
            } else {
                System.out.println("Оголошення з ID " + id + " не знайдено для видалення");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
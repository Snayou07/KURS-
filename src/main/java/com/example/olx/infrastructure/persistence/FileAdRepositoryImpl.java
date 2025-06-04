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
                System.out.println("✓ Оновлено існуюче оголошення: " + ad.getAdId() +
                        " (" + ad.getTitle() + ")");
            } else {
                sessionManager.addAdToState(ad);
                System.out.println("✓ Додано нове оголошення: " + ad.getAdId() +
                        " (" + ad.getTitle() + ")");
            }

            // Зберігаємо стан після кожної зміни з обробкою помилок
            try {
                sessionManager.saveState();
                System.out.println("✓ Стан програми збережено після операції з оголошенням");
            } catch (Exception e) {
                System.err.println("❌ ПОМИЛКА: Не вдалося зберегти стан після створення/оновлення оголошення");
                System.err.println("   Причина: " + e.getMessage());

                // Логуємо деталі помилки для діагностики
                if (e.getCause() != null) {
                    System.err.println("   Деталі: " + e.getCause().getMessage());
                }

                // Спробуємо очистити пошкоджений файл і створити новий
                System.out.println("🔄 Спроба відновлення...");
                try {
                    sessionManager.clearCorruptedStateFile();
                    // Повторна спроба збереження
                    sessionManager.saveState();
                    System.out.println("✅ Стан відновлено та збережено");
                } catch (Exception retryException) {
                    System.err.println("❌ Повторна спроба також не вдалася: " + retryException.getMessage());
                    // Не кидаємо виключення, щоб не блокувати роботу програми
                    System.err.println("⚠ УВАГА: Оголошення створено в пам'яті, але не збережено на диск");
                }
            }

            // Перевіряємо, що оголошення дійсно збережено
            Optional<Ad> savedAd = findById(ad.getAdId());
            if (savedAd.isPresent()) {
                System.out.println("✓ Підтверджено: оголошення знайдено в репозиторії");
                return savedAd.get();
            } else {
                System.err.println("❌ ПОМИЛКА: Оголошення не знайдено після збереження!");
                return ad; // Повертаємо оригінал
            }

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
            Optional<Ad> result = sessionManager.getAdsFromState().stream()
                    .filter(ad -> ad.getAdId().equals(id.trim()))
                    .findFirst();

            if (result.isPresent()) {
                System.out.println("✓ Знайдено оголошення: " + id);
            } else {
                System.out.println("⚠ Оголошення не знайдено: " + id);
            }

            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Ad> findAll() {
        lock.readLock().lock();
        try {
            List<Ad> ads = sessionManager.getAdsFromState();
            System.out.println("📋 Знайдено оголошень в репозиторії: " + ads.size());

            // Додаткова діагностика
            if (!ads.isEmpty()) {
                long activeCount = ads.stream()
                        .filter(ad -> "Активне".equals(ad.getStatus()))
                        .count();
                System.out.println("   Активних оголошень: " + activeCount);

                // Виводимо список для діагностики
                System.out.println("   Список всіх оголошень:");
                for (int i = 0; i < ads.size(); i++) {
                    Ad ad = ads.get(i);
                    System.out.println("     " + (i+1) + ". ID: " + ad.getAdId() +
                            ", Заголовок: '" + ad.getTitle() +
                            "', Статус: " + ad.getStatus());
                }
            }

            return ads;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Ad> findBySellerId(String sellerId) {
        if (sellerId == null || sellerId.trim().isEmpty()) {
            return List.of();
        }

        lock.readLock().lock();
        try {
            List<Ad> result = sessionManager.getAdsFromState().stream()
                    .filter(ad -> ad.getSellerId().equals(sellerId.trim()))
                    .collect(Collectors.toList());

            System.out.println("🔍 Знайдено оголошень для продавця " + sellerId + ": " + result.size());
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Ad> findByCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return List.of();
        }

        lock.readLock().lock();
        try {
            List<Ad> result = sessionManager.getAdsFromState().stream()
                    .filter(ad -> ad.getCategoryId().equals(categoryId.trim()))
                    .collect(Collectors.toList());

            System.out.println("🔍 Знайдено оголошень для категорії " + categoryId + ": " + result.size());
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return;
        }

        lock.writeLock().lock();
        try {
            // Спочатку перевіряємо існування
            Optional<Ad> existingAd = findById(id.trim());
            if (!existingAd.isPresent()) {
                System.out.println("⚠ Оголошення з ID " + id + " не знайдено для видалення");
                return;
            }

            // Видаляємо зі стану
            sessionManager.removeAdFromState(id.trim());

            // Зберігаємо стан з обробкою помилок
            try {
                sessionManager.saveState();
                System.out.println("✓ Оголошення " + id + " видалено та стан збережено");
            } catch (Exception e) {
                System.err.println("❌ Помилка збереження стану після видалення оголошення: " + e.getMessage());

                // Спробуємо відновити
                try {
                    sessionManager.clearCorruptedStateFile();
                    sessionManager.saveState();
                    System.out.println("✅ Стан відновлено після видалення");
                } catch (Exception retryException) {
                    System.err.println("❌ Не вдалося відновити стан: " + retryException.getMessage());
                    System.err.println("⚠ УВАГА: Оголошення видалено з пам'яті, але зміни не збережено на диск");
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Додатковий метод для діагностики
    public void printRepositoryState() {
        lock.readLock().lock();
        try {
            System.out.println("=== СТАН РЕПОЗИТОРІЮ ОГОЛОШЕНЬ ===");
            List<Ad> ads = sessionManager.getAdsFromState();
            System.out.println("Загальна кількість: " + ads.size());

            if (!ads.isEmpty()) {
                System.out.println("Деталі оголошень:");
                for (Ad ad : ads) {
                    System.out.println("- ID: " + ad.getAdId() +
                            ", Заголовок: '" + ad.getTitle() +
                            "', Статус: " + ad.getStatus() +
                            ", Продавець: " + ad.getSellerId());
                }
            }
            System.out.println("================================");
        } finally {
            lock.readLock().unlock();
        }
    }
}
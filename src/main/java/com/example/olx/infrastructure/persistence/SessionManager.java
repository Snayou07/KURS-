// com/example/olx/infrastructure/persistence/SessionManager.java
package com.example.olx.infrastructure.persistence;

import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.AppState;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.model.User;
import com.example.olx.domain.exception.DataPersistenceException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private AppState currentAppState;
    private String filePath = "olx_session_refactored.dat";
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private SessionManager() {
        this.currentAppState = new AppState(); // Початковий порожній стан

        // Автоматично завантажуємо стан при ініціалізації
        try {
            loadState();
            System.out.println("✓ SessionManager ініціалізовано з завантаженням стану");
        } catch (DataPersistenceException e) {
            System.out.println("⚠ Файл стану не знайдено або помилка завантаження. Починаємо з порожнього стану.");
            System.out.println("Деталі: " + e.getMessage());

            // Видаляємо пошкоджений файл
            clearCorruptedStateFile();
        }
    }
    // В SessionManager.java
    public Ad createSerializableAdCopy(Ad original) {
        Ad copy = new Ad(
                original.getTitle(),
                original.getDescription(),
                original.getPrice(),
                original.getCategoryId(),
                original.getSellerId(),
                new ArrayList<>(original.getImagePaths()) // Забезпечити копіювання списку
        );
        copy.setAdId(original.getAdId()); //

        // Встановлюємо ВСІ необхідні поля, що не встановлюються конструктором за замовчуванням
        // або які треба перекрити з 'original'
        copy.setStatus(original.getStatus()); // Встановлює рядок статусу
        copy.setPremium(original.isPremium()); //
        copy.setUrgent(original.isUrgent()); //

        // Копіювання даних про доставку
        if (original.hasDelivery()) { //
            copy.setHasDelivery(true); //
            copy.setFreeDelivery(original.isFreeDelivery()); //
            copy.setDeliveryCost(original.getDeliveryCost()); //
            copy.setDeliveryInfo(original.getDeliveryInfo()); //
        } else {
            copy.setHasDelivery(false);
        }

        // Копіювання даних про гарантію
        if (original.hasWarranty()) { //
            copy.setHasWarranty(true); //
            copy.setWarrantyMonths(original.getWarrantyMonths()); //
            copy.setWarrantyType(original.getWarrantyType()); //
        } else {
            copy.setHasWarranty(false);
        }

        // Копіювання даних про знижку
        if (original.hasDiscount()) { //
            copy.setHasDiscount(true); //
            copy.setDiscountPercentage(original.getDiscountPercentage()); //
            copy.setDiscountReason(original.getDiscountReason()); //
        } else {
            copy.setHasDiscount(false);
        }

        // Важливо: зберегти оригінальні дати створення/оновлення, якщо це потрібно.
        // Конструктор Ad встановлює createdAt і updatedAt на LocalDateTime.now().
        // Якщо потрібно зберегти оригінальні, потрібні сеттери в Ad.java для цих полів.
        // Наприклад: copy.setCreatedAtInternal(original.getCreatedAt());
        //            copy.setUpdatedAtInternal(original.getUpdatedAt());
        // Або, якщо `Ad.clone()` підходить для копіювання, розглянути його.

        return copy;
    }
    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void setStorageFilePath(String filePath) {
        lock.writeLock().lock();
        try {
            this.filePath = filePath;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void printCurrentState() {
        lock.readLock().lock();
        try {
            System.out.println("=== ПОТОЧНИЙ СТАН SESSIONMANAGER ===");
            System.out.println("Файл: " + filePath);
            System.out.println("Користувачів: " + currentAppState.getUsers().size());
            System.out.println("Оголошень: " + currentAppState.getAds().size());
            System.out.println("Категорій: " + currentAppState.getCategories().size());

            if (!currentAppState.getAds().isEmpty()) {
                System.out.println("Список оголошень:");
                for (int i = 0; i < currentAppState.getAds().size(); i++) {
                    Ad ad = currentAppState.getAds().get(i);
                    System.out.println("  " + (i+1) + ". ID: " + ad.getAdId() +
                            ", Заголовок: " + ad.getTitle() +
                            ", Статус: " + ad.getStatus());
                }
            }
            System.out.println("=====================================");
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<User> getUsersFromState() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(currentAppState.getUsers());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void updateUserInState(User userToUpdate) {
        lock.writeLock().lock();
        try {
            List<User> users = currentAppState.getUsers();
            users.removeIf(u -> u.getUserId().equals(userToUpdate.getUserId()));
            users.add(userToUpdate);
            System.out.println("Користувач оновлений в стані: " + userToUpdate.getUserId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addUserToState(User userToAdd) {
        lock.writeLock().lock();
        try {
            currentAppState.getUsers().add(userToAdd);
            System.out.println("Користувач доданий в стан: " + userToAdd.getUserId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeUserFromState(String userId) {
        lock.writeLock().lock();
        try {
            boolean removed = currentAppState.getUsers().removeIf(u -> u.getUserId().equals(userId));
            System.out.println("Користувач видалений зі стану: " + userId + " (успішно: " + removed + ")");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Ad> getAdsFromState() {
        lock.readLock().lock();
        try {
            List<Ad> ads = new ArrayList<>(currentAppState.getAds());
            System.out.println("Запит на отримання оголошень. Поточна кількість: " + ads.size());
            return ads;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void updateAdInState(Ad adToUpdate) {
        lock.writeLock().lock();
        try {
            List<Ad> ads = currentAppState.getAds();
            boolean removed = ads.removeIf(a -> a.getAdId().equals(adToUpdate.getAdId()));
            ads.add(adToUpdate);
            System.out.println("Оголошення оновлено в стані: " + adToUpdate.getAdId() +
                    " (старе видалено: " + removed + ")");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addAdToState(Ad adToAdd) {
        lock.writeLock().lock();
        try {
            currentAppState.getAds().add(adToAdd);
            System.out.println("Оголошення додано в стан: " + adToAdd.getAdId() +
                    ". Загальна кількість: " + currentAppState.getAds().size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeAdFromState(String adId) {
        lock.writeLock().lock();
        try {
            boolean removed = currentAppState.getAds().removeIf(a -> a.getAdId().equals(adId));
            System.out.println("Оголошення видалене зі стану: " + adId + " (успішно: " + removed + ")");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<CategoryComponent> getCategoriesFromState() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(currentAppState.getCategories());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setCategoriesInState(List<CategoryComponent> categories) {
        lock.writeLock().lock();
        try {
            currentAppState.setCategories(new ArrayList<>(categories));
            System.out.println("Категорії встановлені в стан. Кількість: " + categories.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void saveState() throws DataPersistenceException {
        lock.readLock().lock();
        try {
            // Створюємо копію стану для серіалізації
            AppState stateToSave = createSerializableCopy(currentAppState);

            // Створюємо резервну копію існуючого файлу
            File currentFile = new File(filePath);
            File backupFile = new File(filePath + ".backup");

            if (currentFile.exists()) {
                try {
                    java.nio.file.Files.copy(currentFile.toPath(), backupFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    System.err.println("⚠ Не вдалося створити резервну копію: " + e.getMessage());
                }
            }

            // Зберігаємо новий стан
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(filePath)))) {
                oos.writeObject(stateToSave);
                oos.flush();

                System.out.println("✅ Стан збережено у файл: " + filePath);
                System.out.println("   Користувачів: " + stateToSave.getUsers().size());
                System.out.println("   Оголошень: " + stateToSave.getAds().size());
                System.out.println("   Категорій: " + stateToSave.getCategories().size());

                // Видаляємо резервну копію після успішного збереження
                if (backupFile.exists()) {
                    backupFile.delete();
                }

            } catch (IOException e) {
                System.err.println("❌ Помилка збереження стану у файл: " + filePath);

                // Відновлюємо з резервної копії при помилці
                if (backupFile.exists()) {
                    try {
                        java.nio.file.Files.move(backupFile.toPath(), currentFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("✅ Відновлено з резервної копії");
                    } catch (Exception restoreException) {
                        System.err.println("❌ Не вдалося відновити з резервної копії: " +
                                restoreException.getMessage());
                    }
                }

                throw new DataPersistenceException("Error saving session state to file: " + filePath, e);
            }

        } finally {
            lock.readLock().unlock();
        }
    }

    private AppState createSerializableCopy(AppState original) {
        AppState copy = new AppState();

        // Копіюємо користувачів
        for (User user : original.getUsers()) {
            copy.getUsers().add(user); // User повинен бути Serializable
        }

        // Копіюємо оголошення з перевіркою серіалізованості
        for (Ad ad : original.getAds()) {
            try {
                // Створюємо копію оголошення без проблемних полів
                Ad adCopy = createSerializableAdCopy(ad);
                copy.getAds().add(adCopy);
            } catch (Exception e) {
                System.err.println("⚠ Пропущено оголошення при серіалізації: " + ad.getAdId() +
                        " - " + e.getMessage());
            }
        }

        // Копіюємо категорії
        for (CategoryComponent category : original.getCategories()) {
            copy.getCategories().add(category); // CategoryComponent повинен бути Serializable
        }

        return copy;
    }
/*
    private Ad createSerializableAdCopy(Ad original) {
        // Створюємо нове оголошення з базовими даними
        Ad copy = new Ad(
                original.getTitle(),
                original.getDescription(),
                original.getPrice(),
                original.getCategoryId(),
                original.getSellerId(),
                original.getImagePaths()
        );

        // Встановлюємо ID
        copy.setAdId(original.getAdId());

        // Встановлюємо стан через статус (без серіалізації State об'єкта)
        String status = original.getStatus();
        copy.setStatusDirectly(status); // Потрібно додати цей метод в Ad

        return copy;
    }
    
 */

    public void loadState() throws DataPersistenceException {
        lock.writeLock().lock();
        try {
            if (filePath == null) {
                throw new DataPersistenceException("File path not set");
            }

            File file = new File(filePath);
            if (file.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(
                        new BufferedInputStream(new FileInputStream(file)))) {

                    AppState loadedState = (AppState) ois.readObject();
                    this.currentAppState = loadedState;

                    System.out.println("✅ Стан завантажено з файлу: " + filePath);
                    System.out.println("   Користувачів: " + currentAppState.getUsers().size());
                    System.out.println("   Оголошень: " + currentAppState.getAds().size());
                    System.out.println("   Категорій: " + currentAppState.getCategories().size());

                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("❌ Помилка завантаження стану з файлу: " + filePath);

                    if (e instanceof InvalidClassException) {
                        System.err.println("❌ Файл стану несумісний з поточною версією програми");
                        System.err.println("❌ Рекомендується видалити файл: " + filePath);
                    }

                    // Не кидаємо виключення, а просто очищуємо стан
                    clearCorruptedStateFile();
                }
            } else {
                System.out.println("ℹ Файл стану не існує: " + filePath + ". Починаємо з порожнього стану.");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clearCurrentState() {
        lock.writeLock().lock();
        try {
            this.currentAppState.clear();
            System.out.println("Поточний стан програми очищено в SessionManager.");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clearCorruptedStateFile() {
        lock.writeLock().lock();
        try {
            if (filePath != null) {
                File file = new File(filePath);
                if (file.exists()) {
                    if (file.delete()) {
                        System.out.println("✅ Пошкоджений файл стану видалено: " + filePath);
                    } else {
                        System.err.println("❌ Не вдалося видалити пошкоджений файл: " + filePath);
                    }
                }
            }
            this.currentAppState = new AppState(); // Скидаємо до порожнього стану
        } finally {
            lock.writeLock().unlock();
        }
    }
}
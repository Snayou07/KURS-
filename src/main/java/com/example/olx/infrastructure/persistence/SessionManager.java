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

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private AppState currentAppState;
    private String filePath = "olx_session_refactored.dat";

    private SessionManager() {
        this.currentAppState = new AppState(); // Початковий порожній стан

        // ВИПРАВЛЕННЯ: Автоматично завантажуємо стан при ініціалізації
        try {
            loadState();
            System.out.println("✓ SessionManager ініціалізовано з завантаженням стану");
        } catch (DataPersistenceException e) {
            System.out.println("⚠ Файл стану не знайдено або помилка завантаження. Починаємо з порожнього стану.");
            System.out.println("Деталі: " + e.getMessage());
            // Залишаємо порожній стан - це нормально для першого запуску
        }
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void setStorageFilePath(String filePath) {
        this.filePath = filePath;
    }

    // Додаємо метод для діагностики
    public synchronized void printCurrentState() {
        System.out.println("=== ПОТОЧНИЙ СТАН SESSIONMANAGER ===");
        System.out.println("Файл: " + filePath);
        System.out.println("Користувачів: " + currentAppState.getUsers().size());
        System.out.println("Оголошень: " + currentAppState.getAds().size());
        System.out.println("Категорій: " + currentAppState.getCategories().size());

        // Детальна інформація про оголошення
        if (!currentAppState.getAds().isEmpty()) {
            System.out.println("Список оголошень:");
            for (int i = 0; i < currentAppState.getAds().size(); i++) {
                Ad ad = currentAppState.getAds().get(i);
                System.out.println("  " + (i+1) + ". ID: " + ad.getAdId() + ", Заголовок: " + ad.getTitle());
            }
        }
        System.out.println("=====================================");
    }

    public synchronized List<User> getUsersFromState() {
        return new ArrayList<>(currentAppState.getUsers());
    }

    public synchronized void updateUserInState(User userToUpdate) {
        List<User> users = currentAppState.getUsers();
        users.removeIf(u -> u.getUserId().equals(userToUpdate.getUserId()));
        users.add(userToUpdate);
        System.out.println("Користувач оновлений в стані: " + userToUpdate.getUserId());
    }

    public synchronized void addUserToState(User userToAdd) {
        currentAppState.getUsers().add(userToAdd);
        System.out.println("Користувач доданий в стан: " + userToAdd.getUserId());
    }

    public synchronized void removeUserFromState(String userId) {
        boolean removed = currentAppState.getUsers().removeIf(u -> u.getUserId().equals(userId));
        System.out.println("Користувач видалений зі стану: " + userId + " (успішно: " + removed + ")");
    }

    public synchronized List<Ad> getAdsFromState() {
        List<Ad> ads = new ArrayList<>(currentAppState.getAds());
        System.out.println("Запит на отримання оголошень. Поточна кількість: " + ads.size());
        return ads;
    }

    public synchronized void updateAdInState(Ad adToUpdate) {
        List<Ad> ads = currentAppState.getAds();
        boolean removed = ads.removeIf(a -> a.getAdId().equals(adToUpdate.getAdId()));
        ads.add(adToUpdate);
        System.out.println("Оголошення оновлено в стані: " + adToUpdate.getAdId() + " (старе видалено: " + removed + ")");
    }

    public synchronized void addAdToState(Ad adToAdd) {
        currentAppState.getAds().add(adToAdd);
        System.out.println("Оголошення додано в стан: " + adToAdd.getAdId() + ". Загальна кількість: " + currentAppState.getAds().size());
    }

    public synchronized void removeAdFromState(String adId) {
        boolean removed = currentAppState.getAds().removeIf(a -> a.getAdId().equals(adId));
        System.out.println("Оголошення видалене зі стану: " + adId + " (успішно: " + removed + ")");
    }

    public synchronized List<CategoryComponent> getCategoriesFromState() {
        return new ArrayList<>(currentAppState.getCategories());
    }

    public synchronized void setCategoriesInState(List<CategoryComponent> categories) {
        currentAppState.setCategories(new ArrayList<>(categories));
        System.out.println("Категорії встановлені в стан. Кількість: " + categories.size());
    }

    public synchronized void saveState() throws DataPersistenceException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this.currentAppState);
            System.out.println("✅ Стан збережено у файл: " + filePath);
            System.out.println("   Користувачів: " + currentAppState.getUsers().size());
            System.out.println("   Оголошень: " + currentAppState.getAds().size());
            System.out.println("   Категорій: " + currentAppState.getCategories().size());
        } catch (IOException e) {
            System.err.println("❌ Помилка збереження стану у файл: " + filePath);
            throw new DataPersistenceException("Error saving session state to file: " + filePath, e);
        }
    }

    public synchronized void loadState() throws DataPersistenceException {
        if (filePath == null) {
            throw new DataPersistenceException("File path not set");
        }

        File file = new File(filePath);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                this.currentAppState = (AppState) ois.readObject();
                System.out.println("✅ Стан завантажено з файлу: " + filePath);
                System.out.println("   Користувачів: " + currentAppState.getUsers().size());
                System.out.println("   Оголошень: " + currentAppState.getAds().size());
                System.out.println("   Категорій: " + currentAppState.getCategories().size());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("❌ Помилка завантаження стану з файлу: " + filePath);
                throw new DataPersistenceException("Error loading session state from file: " + filePath, e);
            }
        } else {
            System.out.println("ℹ Файл стану не існує: " + filePath + ". Починаємо з порожнього стану.");
        }
    }

    public synchronized void clearCurrentState() {
        this.currentAppState.clear();
        System.out.println("Поточний стан програми очищено в SessionManager.");
    }
}
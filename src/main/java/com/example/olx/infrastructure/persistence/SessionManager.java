// com/example/olx/infrastructure/persistence/SessionManager.java
package com.example.olx.infrastructure.persistence;

import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.AppState; // Припускаємо, що AppState існує
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.model.User;
import com.example.olx.domain.exception.DataPersistenceException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections; // Для потокобезпечних колекцій, якщо потрібно

// SessionManager залишається Singleton
public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private AppState currentAppState;
    private String filePath = "olx_session_refactored.dat"; // Шлях до файлу сесії

    private SessionManager() {
        this.currentAppState = new AppState(); // Початковий порожній стан
        // Важливо: loadFromFile має викликатися явно, наприклад, при старті програми
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void setStorageFilePath(String filePath) {
        this.filePath = filePath;
    }

    // Методи для доступу до даних з AppState для репозиторіїв
    // Ці методи повинні бути потокобезпечними, якщо декілька репозиторіїв їх використовують одночасно
    // Для простоти поки що не додаємо явну синхронізацію, але це важливо у багатопотоковому середовищі.

    public synchronized List<User> getUsersFromState() {
        return new ArrayList<>(currentAppState.getUsers()); // Повертаємо копію
    }

    public synchronized void updateUserInState(User userToUpdate) {
        List<User> users = currentAppState.getUsers();
        users.removeIf(u -> u.getUserId().equals(userToUpdate.getUserId()));
        users.add(userToUpdate);
    }
    public synchronized void addUserToState(User userToAdd) {
        currentAppState.getUsers().add(userToAdd);
    }


    public synchronized void removeUserFromState(String userId) {
        currentAppState.getUsers().removeIf(u -> u.getUserId().equals(userId));
    }

    public synchronized List<Ad> getAdsFromState() {
        return new ArrayList<>(currentAppState.getAds());
    }

    public synchronized void updateAdInState(Ad adToUpdate) {
        List<Ad> ads = currentAppState.getAds();
        ads.removeIf(a -> a.getAdId().equals(adToUpdate.getAdId()));
        ads.add(adToUpdate);
    }
    public synchronized void addAdToState(Ad adToAdd) {
        currentAppState.getAds().add(adToAdd);
    }


    public synchronized void removeAdFromState(String adId) {
        currentAppState.getAds().removeIf(a -> a.getAdId().equals(adId));
    }

    public synchronized List<CategoryComponent> getCategoriesFromState() {
        return new ArrayList<>(currentAppState.getCategories());
    }

    public synchronized void setCategoriesInState(List<CategoryComponent> categories) {
        currentAppState.setCategories(new ArrayList<>(categories)); // Замінюємо весь список
    }


    public synchronized void saveState() throws DataPersistenceException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this.currentAppState);
            System.out.println("Session state saved to " + filePath);
        } catch (IOException e) {
            throw new DataPersistenceException("Error saving session state to file: " + filePath, e);
        }
    }

    public synchronized void loadState() {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("No saved session file found at " + filePath + ". Starting with a new session state.");
            this.currentAppState = new AppState();
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            Object loadedObject = ois.readObject();
            if (loadedObject instanceof AppState) {
                this.currentAppState = (AppState) loadedObject;
                System.out.println("Session state loaded from " + filePath);
            } else {
                System.err.println("Loaded object is not of type AppState. Starting new session state.");
                this.currentAppState = new AppState();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Save file not found: " + filePath + ". Starting a new session state.");
            this.currentAppState = new AppState();
        } catch (IOException | ClassNotFoundException e) {
            // Не кидаємо DataPersistenceException тут, щоб програма могла продовжити з чистим станом
            System.err.println("Error loading session state from file: " + e.getMessage() + ". Starting with a new session state.");
            e.printStackTrace();
            this.currentAppState = new AppState();
        }
    }

    public synchronized void clearCurrentState() {
        this.currentAppState.clear();
        System.out.println("Current application state cleared in SessionManager.");
    }
}
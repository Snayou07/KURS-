package com.example.olx.presentation.gui.util;

import com.example.olx.domain.model.User;

/**
 * Глобальний контекст для зберігання стану додатку
 * Використовує паттерн Singleton для управління сесією користувача
 */
public class GlobalContext {

    private static GlobalContext instance;
    private User loggedInUser;

    // Приватний конструктор для реалізації Singleton
    private GlobalContext() {
        this.loggedInUser = null;
    }

    /**
     * Отримати єдиний екземпляр GlobalContext
     * @return екземпляр GlobalContext
     */
    public static synchronized GlobalContext getInstance() {
        if (instance == null) {
            instance = new GlobalContext();
        }
        return instance;
    }

    /**
     * Встановити поточного авторизованого користувача
     * @param user користувач, який увійшов в систему
     */
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        System.out.println("User logged in: " + (user != null ? user.getUsername() : "null"));
    }

    /**
     * Отримати поточного авторизованого користувача
     * @return поточний користувач або null, якщо не авторизований
     */
    public User getLoggedInUser() {
        return this.loggedInUser;
    }

    /**
     * Очистити дані про авторизованого користувача (вихід з системи)
     */
    public void clearLoggedInUser() {
        String username = this.loggedInUser != null ? this.loggedInUser.getUsername() : "unknown";
        this.loggedInUser = null;
        System.out.println("User logged out: " + username);
    }

    /**
     * Перевірити, чи є користувач авторизованим
     * @return true, якщо користувач увійшов в систему
     */
    public boolean isUserLoggedIn() {
        return this.loggedInUser != null;
    }

    /**
     * Отримати ID поточного користувача
     * @return ID користувача або null, якщо не авторизований
     */
    public String getCurrentUserId() {
        return this.loggedInUser != null ? this.loggedInUser.getUserId() : null;
    }

    /**
     * Отримати ім'я поточного користувача
     * @return ім'я користувача або "Гість", якщо не авторизований
     */
    public String getCurrentUsername() {
        return this.loggedInUser != null ? this.loggedInUser.getUsername() : "Гість";
    }

    /**
     * Очистити весь контекст (повний скид стану)
     */
    public void clearContext() {
        clearLoggedInUser();
        // Тут можна додати очищення інших даних контексту в майбутньому
    }
}
// src/main/java/com/example/olx/presentation/gui/context/GlobalContext.java
package com.example.olx.presentation.gui.context;

import com.example.olx.domain.model.User;

/**
 * Singleton клас для збереження глобального контексту GUI додатку
 */
public class GlobalContext {
    private static GlobalContext instance;
    private User loggedInUser;

    private GlobalContext() {}

    public static GlobalContext getInstance() {
        if (instance == null) {
            instance = new GlobalContext();
        }
        return instance;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public boolean isUserLoggedIn() {
        return loggedInUser != null;
    }

    public void logout() {
        this.loggedInUser = null;
    }
}
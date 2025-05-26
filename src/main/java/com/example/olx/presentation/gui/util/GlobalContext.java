// src/main/java/com/example/olx/presentation/gui/util/GlobalContext.java
package com.example.olx.presentation.gui.util; // Або просто com.example.olx.presentation.gui

import com.example.olx.domain.model.User;

public class GlobalContext {
    private static GlobalContext instance;
    private User loggedInUser;

    private GlobalContext() {}

    public static synchronized GlobalContext getInstance() {
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

    public void clearLoggedInUser() {
        this.loggedInUser = null;
    }
}
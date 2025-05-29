// src/main/java/com/example/olx/application/factory/UserFactory.java
package com.example.olx.application.factory;

import com.example.olx.domain.model.Admin;
import com.example.olx.domain.model.RegisteredUser;
import com.example.olx.domain.model.User;
import com.example.olx.infrastructure.security.PasswordHasher;

public class UserFactory {

    // PasswordHasher передається для хешування пароля
    public static User createUser(String username, String password, String email,
                                  PasswordHasher passwordHasher) {
        if (passwordHasher == null) {
            throw new IllegalArgumentException("PasswordHasher cannot be null.");
        }
        String hashedPassword = passwordHasher.hash(password);



                return new RegisteredUser(username, hashedPassword, email);


    }
}
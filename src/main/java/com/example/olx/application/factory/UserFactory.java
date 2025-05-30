// com/example/olx/application/factory/UserFactory.java
package com.example.olx.application.factory;

import com.example.olx.domain.model.Admin;
import com.example.olx.domain.model.RegisteredUser;
import com.example.olx.domain.model.User;
import com.example.olx.domain.model.UserType;
import com.example.olx.infrastructure.security.PasswordHasher; // Інтерфейс

public class UserFactory {

    private final PasswordHasher passwordHasher;

    // Конструктор для ін'єкції залежності PasswordHasher
    public UserFactory(PasswordHasher passwordHasher) {
        if (passwordHasher == null) {
            throw new IllegalArgumentException("PasswordHasher cannot be null.");
        }
        this.passwordHasher = passwordHasher;
    }

    // Метод для створення користувачів (тепер не статичний)
    public User createUser(UserType userType, String username, String password, String email, String... args) {
        String hashedPassword = passwordHasher.hash(password);

        switch (userType) {
            case REGULAR_USER:
                return new RegisteredUser(username, hashedPassword, email);
            case ADMIN:
                if (args.length > 0 && args[0] != null && !args[0].isEmpty()) {
                    String accessLevel = args[0];
                    return new Admin(username, hashedPassword, email, accessLevel);
                } else {
                    throw new IllegalArgumentException("Access level is required for Admin user.");
                }
            default:
                throw new IllegalArgumentException("Unknown user type: " + userType);
        }
    }
}
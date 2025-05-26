// com/example/olx/application/service/impl/UserServiceImpl.java
package com.example.olx.application.service.impl;

import com.example.olx.application.factory.UserFactory;
import com.example.olx.application.service.port.UserService; // Змінено на UserService
import com.example.olx.domain.exception.DuplicateUserException;
import com.example.olx.domain.exception.InvalidInputException;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.User;
import com.example.olx.domain.model.UserType;
import com.example.olx.domain.repository.UserRepository;
import com.example.olx.infrastructure.security.PasswordHasher;
import java.util.List;

public class UserServiceImpl implements UserService { // Реалізує UserService
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserServiceImpl(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public User registerUser(String username, String password, String email, UserType type, String... args) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.isEmpty() ||
                email == null || email.trim().isEmpty()) {
            throw new InvalidInputException("Username, password, and email cannot be empty.");
        }
        // Валідація формату email (проста)
        if (!email.contains("@") || !email.contains(".")) {
            throw new InvalidInputException("Invalid email format.");
        }


        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUserException("User with username '" + username + "' already exists.");
        }

        User newUser = UserFactory.createUser(type, username, password, email, passwordHasher, args);
        return userRepository.save(newUser);
    }

    @Override
    public User loginUser(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.isEmpty()) {
            throw new InvalidInputException("Username and password cannot be empty.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Invalid username or password.")); // Загальне повідомлення

        if (!passwordHasher.check(password, user.getPasswordHash())) {
            throw new UserNotFoundException("Invalid username or password."); // Загальне повідомлення
        }
        return user;
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID '" + id + "' not found."));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
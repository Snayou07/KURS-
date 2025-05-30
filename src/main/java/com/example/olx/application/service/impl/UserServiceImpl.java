// com/example/olx/application/service/impl/UserServiceImpl.java
package com.example.olx.application.service.impl;

import com.example.olx.application.factory.UserFactory;
import com.example.olx.application.service.port.UserService;
import com.example.olx.domain.exception.DuplicateUserException;
import com.example.olx.domain.exception.InvalidInputException;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.User;
import com.example.olx.domain.model.UserType;
import com.example.olx.domain.repository.UserRepository;
import com.example.olx.infrastructure.security.PasswordHasher;
import java.util.List;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private UserFactory userFactory;

    public UserServiceImpl(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.userFactory = userFactory;
    }

    @Override
    public User registerUser(String username, String password, String email) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.isEmpty() ||
                email == null || email.trim().isEmpty()) {
            throw new InvalidInputException("Username, password, and email cannot be empty.");
        }

        // Валідація формату email
        if (!isValidEmail(email)) {
            throw new InvalidInputException("Invalid email format.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUserException("User with username '" + username + "' already exists.");
        }

        // Використовуємо фабрику для створення користувача
        User newUser = userFactory.createUser(UserType.REGULAR_USER, username, password, email);
        return userRepository.save(newUser);
    }

    @Override
    public User loginUser(String username, String password) throws UserNotFoundException {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.isEmpty()) {
            throw new InvalidInputException("Username and password cannot be empty.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Invalid username or password."));

        if (!passwordHasher.check(password, user.getPasswordHash())) {
            throw new UserNotFoundException("Invalid username or password.");
        }
        return user;
    }

    @Override
    public User getUserById(String id) throws UserNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID '" + id + "' not found."));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void setCurrentUser(User user) {
        // Встановлення поточного користувача
        CurrentUserHolder.setCurrentUser(user);
    }
    /*
    @Override
    public void deleteById(String userId) throws UserNotFoundException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new InvalidInputException("User ID cannot be empty.");
        }

        if (!userRepository.findById(userId).isPresent()) {
            throw new UserNotFoundException("User with ID '" + userId + "' not found.");
        }

        userRepository.deleteById(userId);
    }
*/
    // Допоміжний метод для валідації email
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Покращена валідація email
        return email.contains("@") && email.contains(".") &&
                email.indexOf("@") < email.lastIndexOf(".") &&
                email.indexOf("@") > 0 &&
                email.lastIndexOf(".") < email.length() - 1;
    }

    // Допоміжний клас для зберігання поточного користувача
    private static class CurrentUserHolder {
        private static User currentUser;

        public static void setCurrentUser(User user) {
            currentUser = user;
        }

        public static User getCurrentUser() {
            return currentUser;
        }
    }
}
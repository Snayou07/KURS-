package com.example.olx.presentation.gui.controller;

import com.example.olx.domain.exception.DuplicateUserException;
import com.example.olx.domain.exception.InvalidInputException;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.User;
import com.example.olx.domain.model.UserType;
import com.example.olx.presentation.gui.MainGuiApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TabPane; // Для переключення вкладок, якщо потрібно

import java.io.IOException;

public class LoginController {

    // Поля для входу
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Button loginButton;
    @FXML private Label loginErrorLabel;

    // Поля для реєстрації
    @FXML private TextField registerUsernameField;
    @FXML private TextField registerEmailField;
    @FXML private PasswordField registerPasswordField;
    @FXML private TextField registerUserTypeField; // REGULAR_USER or ADMIN
    @FXML private Label adminAccessCodeLabel;
    @FXML private TextField adminAccessCodeField; // Для простоти, просто текстове поле
    @FXML private Button registerButton;
    @FXML private Label registerErrorLabel;

    @FXML private TabPane tabPane; // Якщо потрібно програмно переключати вкладки

    @FXML
    public void initialize() {
        // Початкове налаштування, якщо потрібно
        loginErrorLabel.setText("");
        registerErrorLabel.setText("");

        // Показувати поле коду доступу тільки якщо вибрано ADMIN
        registerUserTypeField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAdmin = UserType.ADMIN.toString().equalsIgnoreCase(newVal.trim());
            adminAccessCodeLabel.setVisible(isAdmin);
            adminAccessCodeField.setVisible(isAdmin);
        });
    }

    @FXML
    private void handleLogin() {
        String username = loginUsernameField.getText();
        String password = loginPasswordField.getText();
        loginErrorLabel.setText("");

        if (username.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Ім'я користувача та пароль не можуть бути порожніми.");
            return;
        }

        try {
            User loggedInUser = MainGuiApp.userService.loginUser(username, password);
            // Успішний вхід - зберігаємо користувача в контексті (поки що просто статичне поле)
            // і переходимо на головний екран
            GlobalContext.getInstance().setLoggedInUser(loggedInUser); // Використання Singleton для контексту
            MainGuiApp.loadMainScene();
        } catch (UserNotFoundException e) {
            loginErrorLabel.setText("Неправильний логін або пароль.");
        } catch (InvalidInputException e) {
            loginErrorLabel.setText(e.getMessage());
        } catch (IOException e) {
            loginErrorLabel.setText("Помилка завантаження головної сцени: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            loginErrorLabel.setText("Невідома помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        String username = registerUsernameField.getText();
        String email = registerEmailField.getText();
        String password = registerPasswordField.getText();
        String userTypeStr = registerUserTypeField.getText().trim().toUpperCase();
        String adminCode = adminAccessCodeField.getText(); // Поки не використовуємо, але можна додати перевірку

        registerErrorLabel.setText("");

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || userTypeStr.isEmpty()) {
            registerErrorLabel.setText("Всі поля (окрім коду адміна) є обов'язковими.");
            return;
        }

        UserType userType;
        try {
            userType = UserType.valueOf(userTypeStr);
        } catch (IllegalArgumentException e) {
            registerErrorLabel.setText("Неправильний тип користувача. Доступні: REGULAR_USER, ADMIN");
            return;
        }

        // Проста перевірка "коду доступу" для адміна (можна зробити складнішою)
        String accessLevelArg = null;
        if (userType == UserType.ADMIN) {
            if (adminCode.isEmpty()) {
                registerErrorLabel.setText("Для ADMIN потрібен код доступу (access level).");
                return;
            }
            accessLevelArg = adminCode; // Використовуємо введений код як рівень доступу
        }


        try {
            MainGuiApp.userService.registerUser(username, password, email, userType, accessLevelArg);
            registerErrorLabel.setStyle("-fx-text-fill: green;");
            registerErrorLabel.setText("Реєстрація успішна! Тепер ви можете увійти.");
            // Очистити поля або переключити на вкладку входу
            // tabPane.getSelectionModel().selectFirst(); // Переключити на вкладку входу
            clearRegistrationFields();
        } catch (DuplicateUserException e) {
            registerErrorLabel.setText("Користувач з таким ім'ям вже існує.");
        } catch (InvalidInputException e) {
            registerErrorLabel.setText(e.getMessage());
        } catch (Exception e) {
            registerErrorLabel.setText("Помилка реєстрації: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearRegistrationFields() {
        registerUsernameField.clear();
        registerEmailField.clear();
        registerPasswordField.clear();
        registerUserTypeField.setText("REGULAR_USER"); // Повертаємо до значення за замовчуванням
        adminAccessCodeField.clear();
    }
}
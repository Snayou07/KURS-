package com.example.olx.presentation.gui.controller;

import com.example.olx.domain.exception.DuplicateUserException;
import com.example.olx.domain.exception.InvalidInputException;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.User;
import com.example.olx.domain.model.UserType;
import com.example.olx.presentation.gui.MainGuiApp;
import com.example.olx.presentation.gui.util.GlobalContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TabPane;

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
        // Початкове налаштування
        loginErrorLabel.setText("");
        registerErrorLabel.setText("");
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
            // Успішний вхід - зберігаємо користувача в контексті
            GlobalContext.getInstance().setLoggedInUser(loggedInUser);

            // Додаємо відладочний вивід для перевірки
            System.out.println("User logged in successfully: " + loggedInUser.getUsername());

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

        registerErrorLabel.setText("");
        registerErrorLabel.setStyle("-fx-text-fill: red;"); // Червоний колір для помилок

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            registerErrorLabel.setText("Всі поля є обов'язковими.");
            return;
        }

        try {
            // Викликаємо реєстрацію з основними параметрами
            MainGuiApp.userService.registerUser(username, password, email);

            registerErrorLabel.setStyle("-fx-text-fill: green;");
            registerErrorLabel.setText("Реєстрація успішна! Тепер ви можете увійти.");

            // Очистити поля реєстрації
            clearRegistrationFields();

            // Опціонально: переключити на вкладку входу
            if (tabPane != null) {
                tabPane.getSelectionModel().selectFirst();
            }

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
    }
}
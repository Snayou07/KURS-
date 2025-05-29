package com.example.olx.presentation.gui.util;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Утилітний клас для валідації форм та стилізації полів
 */
public class ValidationUtils {

    /**
     * Створює контейнер з міткою та зірочкою для обов'язкового поля
     */
    public static VBox createRequiredFieldContainer(String labelText) {
        VBox container = new VBox(5);
        container.getStyleClass().add("field-container");

        HBox labelBox = new HBox(3);
        labelBox.getStyleClass().add("field-row");

        Label label = new Label(labelText);
        label.getStyleClass().add("required-field-label");

        Label asterisk = new Label("*");
        asterisk.getStyleClass().add("required-asterisk");

        labelBox.getChildren().addAll(label, asterisk);
        container.getChildren().add(labelBox);

        return container;
    }

    /**
     * Створює контейнер з міткою для необов'язкового поля
     */
    public static VBox createFieldContainer(String labelText) {
        VBox container = new VBox(5);
        container.getStyleClass().add("field-container");

        Label label = new Label(labelText);
        label.getStyleClass().add("required-field-label");

        container.getChildren().add(label);

        return container;
    }

    /**
     * Додає стиль помилки до поля
     */
    public static void addErrorStyle(Control control) {
        control.getStyleClass().removeAll("valid", "error");
        if (!control.getStyleClass().contains("error")) {
            control.getStyleClass().add("error");
        }
    }

    /**
     * Додає стиль успішної валідації до поля
     */
    public static void addValidStyle(Control control) {
        control.getStyleClass().removeAll("error", "valid");
        if (!control.getStyleClass().contains("valid")) {
            control.getStyleClass().add("valid");
        }
    }

    /**
     * Видаляє всі стилі валідації з поля
     */
    public static void removeValidationStyles(Control control) {
        control.getStyleClass().removeAll("error", "valid");
    }

    /**
     * Показує tooltip з повідомленням про помилку
     */
    public static void showErrorTooltip(Control control, String message) {
        Tooltip errorTooltip = new Tooltip(message);
        errorTooltip.getStyleClass().add("error-tooltip");
        errorTooltip.setShowDelay(javafx.util.Duration.millis(100));
        errorTooltip.setHideDelay(javafx.util.Duration.millis(3000));
        control.setTooltip(errorTooltip);
    }

    /**
     * Приховує tooltip помилки
     */
    public static void hideErrorTooltip(Control control) {
        control.setTooltip(null);
    }

    /**
     * Валідує обов'язкове текстове поле
     */
    public static boolean validateRequiredTextField(TextField field, String fieldName) {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            addErrorStyle(field);
            showErrorTooltip(field, fieldName + " є обов'язковим полем");
            return false;
        } else {
            addValidStyle(field);
            hideErrorTooltip(field);
            return true;
        }
    }

    /**
     * Валідує обов'язкове текстове поле (TextArea)
     */
    public static boolean validateRequiredTextArea(TextArea field, String fieldName) {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            addErrorStyle(field);
            showErrorTooltip(field, fieldName + " є обов'язковим полем");
            return false;
        } else {
            addValidStyle(field);
            hideErrorTooltip(field);
            return true;
        }
    }

    /**
     * Валідує обов'язковий ComboBox
     */
    public static boolean validateRequiredComboBox(ComboBox<?> comboBox, String fieldName) {
        if (comboBox.getValue() == null) {
            addErrorStyle(comboBox);
            showErrorTooltip(comboBox, "Оберіть " + fieldName.toLowerCase());
            return false;
        } else {
            addValidStyle(comboBox);
            hideErrorTooltip(comboBox);
            return true;
        }
    }

    /**
     * Валідує email адресу
     */
    public static boolean validateEmail(TextField emailField) {
        String email = emailField.getText();
        if (email == null || email.trim().isEmpty()) {
            addErrorStyle(emailField);
            showErrorTooltip(emailField, "Email є обов'язковим полем");
            return false;
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            addErrorStyle(emailField);
            showErrorTooltip(emailField, "Введіть коректну email адресу");
            return false;
        }

        addValidStyle(emailField);
        hideErrorTooltip(emailField);
        return true;
    }

    /**
     * Валідує ціну
     */
    public static boolean validatePrice(TextField priceField) {
        String priceText = priceField.getText();
        if (priceText == null || priceText.trim().isEmpty()) {
            addErrorStyle(priceField);
            showErrorTooltip(priceField, "Ціна є обов'язковим полем");
            return false;
        }

        try {
            double price = Double.parseDouble(priceText.replace(",", "."));
            if (price <= 0) {
                addErrorStyle(priceField);
                showErrorTooltip(priceField, "Ціна повинна бути більше 0");
                return false;
            }
            if (price > 1000000) {
                addErrorStyle(priceField);
                showErrorTooltip(priceField, "Ціна занадто велика");
                return false;
            }

            addValidStyle(priceField);
            hideErrorTooltip(priceField);
            return true;
        } catch (NumberFormatException e) {
            addErrorStyle(priceField);
            showErrorTooltip(priceField, "Введіть коректну ціну (тільки цифри)");
            return false;
        }
    }

    /**
     * Валідує телефонний номер
     */
    public static boolean validatePhone(TextField phoneField) {
        String phone = phoneField.getText();
        if (phone == null || phone.trim().isEmpty()) {
            addErrorStyle(phoneField);
            showErrorTooltip(phoneField, "Номер телефону є обов'язковим");
            return false;
        }

        // Український формат: +380XXXXXXXXX або 0XXXXXXXXX
        String phoneRegex = "^(\\+380|0)[0-9]{9}$";
        if (!phone.replaceAll("[\\s()-]", "").matches(phoneRegex)) {
            addErrorStyle(phoneField);
            showErrorTooltip(phoneField, "Введіть коректний номер телефону");
            return false;
        }

        addValidStyle(phoneField);
        hideErrorTooltip(phoneField);
        return true;
    }

    /**
     * Валідує пароль
     */
    public static boolean validatePassword(PasswordField passwordField) {
        String password = passwordField.getText();
        if (password == null || password.isEmpty()) {
            addErrorStyle(passwordField);
            showErrorTooltip(passwordField, "Пароль є обов'язковим");
            return false;
        }

        if (password.length() < 6) {
            addErrorStyle(passwordField);
            showErrorTooltip(passwordField, "Пароль повинен містити мінімум 6 символів");
            return false;
        }

        addValidStyle(passwordField);
        hideErrorTooltip(passwordField);
        return true;
    }

    /**
     * Валідує підтвердження пароля
     */
    public static boolean validatePasswordConfirm(PasswordField passwordField, PasswordField confirmField) {
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        if (confirm == null || confirm.isEmpty()) {
            addErrorStyle(confirmField);
            showErrorTooltip(confirmField, "Підтвердіть пароль");
            return false;
        }

        if (!password.equals(confirm)) {
            addErrorStyle(confirmField);
            showErrorTooltip(confirmField, "Паролі не співпадають");
            return false;
        }

        addValidStyle(confirmField);
        hideErrorTooltip(confirmField);
        return true;
    }

    /**
     * Очищає валідацію при введенні тексту
     */
    public static void setupRealTimeValidation(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                removeValidationStyles(field);
                hideErrorTooltip(field);
            }
        });
    }

    /**
     * Очищає валідацію при введенні тексту (TextArea)
     */
    public static void setupRealTimeValidation(TextArea field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                removeValidationStyles(field);
                hideErrorTooltip(field);
            }
        });
    }

    /**
     * Очищає валідацію при виборі значення (ComboBox)
     */
    public static void setupRealTimeValidation(ComboBox<?> comboBox) {
        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                removeValidationStyles(comboBox);
                hideErrorTooltip(comboBox);
            }
        });
    }
}
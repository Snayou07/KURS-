// src/main/java/com/example/olx/presentation/gui/controller/AdDetailController.java
package com.example.olx.presentation.gui.controller;

import com.example.olx.domain.exception.AdNotFoundException;
import com.example.olx.domain.exception.UnauthorizedActionException;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.model.User;
import com.example.olx.domain.model.UserType;
import com.example.olx.presentation.gui.MainGuiApp;
import com.example.olx.presentation.gui.util.GlobalContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image; // Для зображень
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Optional;

public class AdDetailController {

    @FXML private Label titleLabel;
    @FXML private ImageView adImageView; // Для майбутніх зображень
    @FXML private Label noImageLabel;
    @FXML private Label priceLabel;
    @FXML private Text descriptionText;
    @FXML private Label categoryLabel;
    @FXML private Label sellerLabel;
    @FXML private Label adIdLabel;
    @FXML private Button backButton;
    @FXML private HBox actionButtonsBox; // Контейнер для кнопок редагування/видалення
    @FXML private Button editButton;
    @FXML private Button deleteButton;


    private Ad currentAd;

    public void initialize() {
        // Початкове приховування кнопок, вони стануть видимими після завантаження даних
        editButton.setVisible(false);
        deleteButton.setVisible(false);
        // TODO: Налаштувати ImageView, якщо є дефолтне зображення "не знайдено"
        // adImageView.setImage(new Image(getClass().getResourceAsStream("/path/to/default-image.png")));
    }

    // Метод для передачі даних про оголошення в цей контролер
    public void initData(Ad ad) {
        if (ad == null) {
            showErrorAndGoBack("Не вдалося завантажити деталі оголошення (дані не передано).");
            return;
        }
        this.currentAd = ad;
        populateAdDetails();
        setupActionButtons();
    }

    private void populateAdDetails() {
        titleLabel.setText(currentAd.getTitle());
        // TODO: Завантаження та відображення реального зображення
        // if (currentAd.getImagePath() != null && !currentAd.getImagePath().isEmpty()) {
        //     try {
        //         adImageView.setImage(new Image(currentAd.getImagePath())); // Або new FileInputStream(path)
        //         adImageView.setVisible(true);
        //         noImageLabel.setVisible(false);
        //     } catch (Exception e) {
        //         System.err.println("Error loading image: " + currentAd.getImagePath() + " - " + e.getMessage());
        //         adImageView.setVisible(false);
        //         noImageLabel.setText("Помилка завантаження фото");
        //         noImageLabel.setVisible(true);
        //     }
        // } else {
        adImageView.setVisible(false);
        noImageLabel.setText("Фото не завантажено");
        noImageLabel.setVisible(true);
        // }

        priceLabel.setText(String.format("%.2f грн", currentAd.getPrice()));
        descriptionText.setText(currentAd.getDescription() != null ? currentAd.getDescription() : "Опис відсутній.");
        adIdLabel.setText(currentAd.getAdId());

        Optional<CategoryComponent> catOpt = MainGuiApp.categoryService.findCategoryById(currentAd.getCategoryId());
        categoryLabel.setText(catOpt.map(CategoryComponent::getName).orElse("Невідома категорія"));

        try {
            User seller = MainGuiApp.userService.getUserById(currentAd.getSellerId());
            sellerLabel.setText(seller.getUsername());
        } catch (Exception e) {
            sellerLabel.setText("Невідомий продавець");
        }
    }

    private void setupActionButtons() {
        User loggedInUser = GlobalContext.getInstance().getLoggedInUser();
        if (loggedInUser != null && currentAd != null) {
            boolean isOwner = loggedInUser.getUserId().equals(currentAd.getSellerId());
            boolean isAdmin = loggedInUser.getUserType() == UserType.ADMIN;

            if (isOwner) {
                editButton.setVisible(true);
            }
            if (isOwner || isAdmin) {
                deleteButton.setVisible(true);
            }
        }
    }

    @FXML
    private void handleEditAd() {
        if (currentAd == null) return;
        System.out.println("Edit Ad: " + currentAd.getTitle());
        // TODO: Перехід на сцену редагування, передаючи currentAd
        // Наприклад, можна адаптувати CreateAdController для режиму редагування
        // MainGuiApp.loadEditAdScene(currentAd); // Потрібно створити такий метод в MainGuiApp
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Редагування");
        alert.setHeaderText(null);
        alert.setContentText("Функція редагування для '" + currentAd.getTitle() + "' ще не реалізована.");
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteAd() {
        if (currentAd == null) return;
        User loggedInUser = GlobalContext.getInstance().getLoggedInUser();
        if (loggedInUser == null) {
            showErrorAlert("Помилка", "Ви не авторизовані для цієї дії.");
            return;
        }

        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Підтвердження видалення");
        confirmationDialog.setHeaderText("Ви дійсно бажаєте видалити оголошення '" + currentAd.getTitle() + "'?");
        confirmationDialog.setContentText("Цю дію неможливо буде скасувати.");

        Optional<ButtonType> result = confirmationDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                MainGuiApp.adService.deleteAd(currentAd.getAdId(), loggedInUser.getUserId());
                showInfoAlert("Успіх", "Оголошення '" + currentAd.getTitle() + "' було успішно видалено.");
                handleBack(); // Повернутися до списку
            } catch (AdNotFoundException e) {
                showErrorAlert("Помилка видалення", "Оголошення не знайдено. Можливо, його вже видалили.");
            } catch (UnauthorizedActionException e) {
                showErrorAlert("Помилка доступу", "Ви не маєте прав для видалення цього оголошення.");
            } catch (Exception e) {
                showErrorAlert("Помилка видалення", "Сталася непередбачена помилка: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    @FXML
    private void handleBack() {
        try {
            MainGuiApp.loadMainScene();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка навігації", "Не вдалося повернутися до головного списку.");
        }
    }

    private void showErrorAndGoBack(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        handleBack(); // Спробувати повернутися назад
    }
    @FXML
    private void handleEditAd() {
        if (currentAd == null) return;
        try {
            MainGuiApp.loadEditAdScene(currentAd); // Викликаємо новий метод
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка редагування", "Не вдалося завантажити форму редагування: " + e.getMessage());
        }
    }
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
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
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AdDetailController {

    @FXML private Label titleLabel;
    @FXML private ImageView adImageView; // Основне зображення
    @FXML private Label noImageLabel;
    @FXML private Label priceLabel;
    @FXML private Text descriptionText;
    @FXML private Label categoryLabel;
    @FXML private Label sellerLabel;
    @FXML private Label adIdLabel;
    @FXML private Button backButton;
    @FXML private HBox actionButtonsBox;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    // Додаткові елементи для галереї фотографій
    private VBox imageGalleryContainer;
    private HBox thumbnailContainer;
    private ImageView currentMainImage;
    private int currentImageIndex = 0;
    private List<String> imagePaths;

    private Ad currentAd;

    public void initialize() {
        editButton.setVisible(true);
        deleteButton.setVisible(true);
        setupImageGallery();
    }

    private void setupImageGallery() {
        // Створюємо контейнер для галереї зображень
        imageGalleryContainer = new VBox(10);
        imageGalleryContainer.setAlignment(Pos.CENTER);

        // Налаштовуємо основне зображення
        currentMainImage = adImageView;
        currentMainImage.setFitHeight(300.0);
        currentMainImage.setFitWidth(400.0);
        currentMainImage.setPreserveRatio(true);
        currentMainImage.setSmooth(true);

        // Створюємо контейнер для мініатюр
        thumbnailContainer = new HBox(10);
        thumbnailContainer.setAlignment(Pos.CENTER);
        thumbnailContainer.setPadding(new Insets(10, 0, 10, 0));

        // Створюємо ScrollPane для мініатюр (якщо їх багато)
        ScrollPane thumbnailScrollPane = new ScrollPane(thumbnailContainer);
        thumbnailScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        thumbnailScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        thumbnailScrollPane.setFitToHeight(true);
        thumbnailScrollPane.setPrefHeight(100);
        thumbnailScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Додаємо ScrollPane до контейнера галереї
        imageGalleryContainer.getChildren().add(thumbnailScrollPane);
    }

    public void initData(Ad ad) {
        if (ad == null) {
            showErrorAndGoBack("Не вдалося завантажити деталі оголошення (дані не передано).");
            return;
        }
        this.currentAd = ad;
        populateAdDetails();
        setupActionButtons();
        loadImages();
    }

    private void populateAdDetails() {
        titleLabel.setText(currentAd.getTitle());
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

    private void loadImages() {
        // Отримуємо список шляхів до зображень з оголошення
        imagePaths = currentAd.getImagePaths();

        if (imagePaths == null || imagePaths.isEmpty()) {
            // Якщо немає зображень, показуємо повідомлення
            showNoImageMessage();
            return;
        }

        // Завантажуємо та відображаємо зображення
        loadMainImage(0);
        loadThumbnails();

        // Додаємо галерею до основного контейнера, якщо більше одного зображення
        if (imagePaths.size() > 1) {
            addGalleryToMainContainer();
        }
    }

    private void showNoImageMessage() {
        adImageView.setVisible(false);
        noImageLabel.setText("Фото не завантажено");
        noImageLabel.setVisible(true);

        // Очищуємо контейнер мініатюр
        thumbnailContainer.getChildren().clear();
    }

    private void loadMainImage(int index) {
        if (index < 0 || index >= imagePaths.size()) {
            return;
        }

        String imagePath = imagePaths.get(index);
        Image image = loadImageFromPath(imagePath);

        if (image != null) {
            currentMainImage.setImage(image);
            currentMainImage.setVisible(true);
            noImageLabel.setVisible(false);
            currentImageIndex = index;
        } else {
            // Якщо не вдалося завантажити зображення, спробуємо наступне
            if (index + 1 < imagePaths.size()) {
                loadMainImage(index + 1);
            } else {
                showNoImageMessage();
            }
        }
    }

    private void loadThumbnails() {
        thumbnailContainer.getChildren().clear();

        for (int i = 0; i < imagePaths.size(); i++) {
            String imagePath = imagePaths.get(i);
            Image thumbnailImage = loadImageFromPath(imagePath);

            if (thumbnailImage != null) {
                ImageView thumbnail = createThumbnail(thumbnailImage, i);
                thumbnailContainer.getChildren().add(thumbnail);
            }
        }
    }

    private ImageView createThumbnail(Image image, int index) {
        ImageView thumbnail = new ImageView(image);
        thumbnail.setFitHeight(80);
        thumbnail.setFitWidth(80);
        thumbnail.setPreserveRatio(true);
        thumbnail.setSmooth(true);

        // Додаємо стиль для вибраної мініатюри
        updateThumbnailStyle(thumbnail, index == currentImageIndex);

        // Додаємо обробник клацання
        thumbnail.setOnMouseClicked(event -> {
            loadMainImage(index);
            updateAllThumbnailStyles();
        });

        // Додаємо ефект при наведенні
        thumbnail.setOnMouseEntered(event -> {
            if (index != currentImageIndex) {
                thumbnail.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 5, 0.5, 0, 0); -fx-cursor: hand;");
            }
        });

        thumbnail.setOnMouseExited(event -> {
            updateThumbnailStyle(thumbnail, index == currentImageIndex);
        });

        return thumbnail;
    }

    private void updateThumbnailStyle(ImageView thumbnail, boolean isSelected) {
        if (isSelected) {
            thumbnail.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,100,255,0.8), 8, 0.6, 0, 0); -fx-cursor: default;");
        } else {
            thumbnail.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0.3, 0, 0); -fx-cursor: hand;");
        }
    }

    private void updateAllThumbnailStyles() {
        for (int i = 0; i < thumbnailContainer.getChildren().size(); i++) {
            if (thumbnailContainer.getChildren().get(i) instanceof ImageView) {
                ImageView thumbnail = (ImageView) thumbnailContainer.getChildren().get(i);
                updateThumbnailStyle(thumbnail, i == currentImageIndex);
            }
        }
    }

    private Image loadImageFromPath(String imagePath) {
        try {
            File imageFile = new File(imagePath);

            // Якщо файл не існує за абсолютним шляхом, спробуємо відносний шлях
            if (!imageFile.exists()) {
                imageFile = new File("user_images", new File(imagePath).getName());
            }

            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString());
            } else {
                System.err.println("Image file not found: " + imagePath);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath + " - " + e.getMessage());
            return null;
        }
    }

    private void addGalleryToMainContainer() {
        // Знаходимо батьківський контейнер основного зображення
        if (adImageView.getParent() instanceof VBox) {
            VBox parentContainer = (VBox) adImageView.getParent();

            // Знаходимо індекс основного зображення
            int imageViewIndex = parentContainer.getChildren().indexOf(adImageView);

            // Додаємо галерею мініатюр після основного зображення
            if (imageViewIndex >= 0 && imageViewIndex + 1 < parentContainer.getChildren().size()) {
                parentContainer.getChildren().add(imageViewIndex + 1, imageGalleryContainer.getChildren().get(0));
            }
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
        try {
            MainGuiApp.loadEditAdScene(currentAd);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка редагування", "Не вдалося завантажити форму редагування: " + e.getMessage());
        }
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
                handleBack();
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
        handleBack();
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
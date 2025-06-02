// src/main/java/com/example/olx/presentation/gui/controller/AdDetailController.java
package com.example.olx.presentation.gui.controller;

import com.example.olx.domain.decorator.AdComponent;
import com.example.olx.domain.decorator.AdDecoratorFactory;
import com.example.olx.domain.exception.AdNotFoundException;
import com.example.olx.domain.exception.UnauthorizedActionException;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.model.User;
import com.example.olx.domain.model.UserType;
import com.example.olx.presentation.gui.MainGuiApp;
import com.example.olx.presentation.gui.util.GlobalContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    @FXML private ImageView adImageView;
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

    // Додаткові елементи для декорованої інформації
    private VBox decoratedInfoContainer;
    private Text decoratedInfoText;

    // Галерея зображень
    private VBox imageGalleryContainer;
    private HBox thumbnailContainer;
    private ImageView currentMainImage;
    private int currentImageIndex = 0;
    private List<String> imagePaths;

    // Основний контейнер з FXML
    @FXML
    private VBox mainContainer;

    private Ad currentAd;
    private AdComponent decoratedAd;

    public void initialize() {
        editButton.setVisible(false);
        deleteButton.setVisible(false);
        setupImageGallery();
        setupDecoratedInfoContainer();

        // Знаходимо основний контейнер
        findMainContainer();
    }

    private void findMainContainer() {
        // Знаходимо основний VBox контейнер через батьківську структуру
        if (titleLabel.getParent() instanceof VBox) {
            mainContainer = (VBox) titleLabel.getParent();
        }
    }

    private void setupDecoratedInfoContainer() {
        // Створюємо контейнер для декорованої інформації
        decoratedInfoContainer = new VBox(10);
        decoratedInfoContainer.setAlignment(Pos.CENTER_LEFT);
        decoratedInfoContainer.setPadding(new Insets(15));
        decoratedInfoContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #e8f4fd, #f8fbff); " +
                        "-fx-border-color: #4a90e2; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(74, 144, 226, 0.3), 8, 0.6, 0, 2);"
        );

        // Створюємо текстовий елемент для декорованої інформації
        decoratedInfoText = new Text();
        decoratedInfoText.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-fill: #2c3e50; " +
                        "-fx-font-family: 'System';"
        );
        decoratedInfoText.setWrappingWidth(700.0);

        // Заголовок для декорованої інформації
        Label decoratedInfoLabel = new Label("✨ Спеціальні умови та особливості:");
        decoratedInfoLabel.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-padding: 0 0 10 0;"
        );

        decoratedInfoContainer.getChildren().addAll(decoratedInfoLabel, decoratedInfoText);
    }

    private void setupImageGallery() {
        imageGalleryContainer = new VBox(10);
        imageGalleryContainer.setAlignment(Pos.CENTER);

        currentMainImage = adImageView;
        currentMainImage.setFitHeight(300.0);
        currentMainImage.setFitWidth(400.0);
        currentMainImage.setPreserveRatio(true);
        currentMainImage.setSmooth(true);

        thumbnailContainer = new HBox(10);
        thumbnailContainer.setAlignment(Pos.CENTER);
        thumbnailContainer.setPadding(new Insets(10, 0, 10, 0));

        ScrollPane thumbnailScrollPane = new ScrollPane(thumbnailContainer);
        thumbnailScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        thumbnailScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        thumbnailScrollPane.setFitToHeight(true);
        thumbnailScrollPane.setPrefHeight(100);
        thumbnailScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        imageGalleryContainer.getChildren().add(thumbnailScrollPane);
    }

    public void initData(Ad ad) {
        initData(ad, false, false, null, null, null, null, null, null, null);
    }

    /**
     * Ініціалізація з декораторами
     */
    public void initData(Ad ad, boolean isPremium, boolean isUrgent,
                         Double discountPercentage, String discountReason,
                         Integer warrantyMonths, String warrantyType,
                         Boolean freeDelivery, Double deliveryCost, String deliveryInfo) {
        if (ad == null) {
            showErrorAndGoBack("Не вдалося завантажити деталі оголошення (дані не передано).");
            return;
        }

        this.currentAd = ad;

        // Створюємо декорований компонент
        this.decoratedAd = AdDecoratorFactory.createFullyDecoratedAd(ad,
                isPremium,
                isUrgent,
                discountPercentage,
                discountReason,
                warrantyMonths,
                warrantyType,
                freeDelivery,
                deliveryCost,
                deliveryInfo);

        populateAdDetails();
        setupActionButtons();
        loadImages();
        displayDecoratedInfo();
    }

    /**
     * Альтернативний метод для швидкого створення декорованого оголошення
     */
    public void initDataWithAutoDecorators(Ad ad) {
        // Автоматично визначаємо декоратори на основі властивостей оголошення
        boolean isPremium = ad.getTitle().toLowerCase().contains("преміум") ||
                (ad.getDescription() != null && ad.getDescription().toLowerCase().contains("преміум"));

        boolean isUrgent = ad.getTitle().toLowerCase().contains("терміново") ||
                (ad.getDescription() != null && ad.getDescription().toLowerCase().contains("терміново"));

        Double discount = null;
        String discountReason = null;
        if (ad.getDescription() != null && ad.getDescription().toLowerCase().contains("знижка")) {
            discount = 15.0; // 15% знижка за замовчуванням
            discountReason = "Сезонна розпродаж";
        }

        Integer warranty = null;
        String warrantyType = null;
        if (ad.getDescription() != null && ad.getDescription().toLowerCase().contains("гарантія")) {
            warranty = 12; // 12 місяців за замовчуванням
            warrantyType = "Офіційна гарантія виробника";
        }

        Boolean freeDelivery = null;
        Double deliveryCost = null;
        String deliveryInfo = null;
        if (ad.getDescription() != null &&
                (ad.getDescription().toLowerCase().contains("доставка") ||
                        ad.getDescription().toLowerCase().contains("безкоштовна доставка"))) {
            if (ad.getDescription().toLowerCase().contains("безкоштовна")) {
                freeDelivery = true;
                deliveryInfo = "Безкоштовна доставка по всій Україні протягом 2-3 робочих днів";
            } else {
                freeDelivery = false;
                deliveryCost = 50.0;
                deliveryInfo = "Доставка Новою Поштою або кур'єрською службою";
            }
        }

        initData(ad, isPremium, isUrgent, discount, discountReason,
                warranty, warrantyType, freeDelivery, deliveryCost, deliveryInfo);
    }

    /**
     * Метод для тестування декораторів з випадковими значеннями
     */
    public void initDataWithTestDecorators(Ad ad) {
        initData(ad, true, true, 20.0, "Розпродаж залишків",
                24, "Розширена гарантія", true, 0.0, "Експрес доставка безкоштовно");
    }

    private void populateAdDetails() {
        // Використовуємо декорований заголовок
        titleLabel.setText(decoratedAd.getFormattedTitle());

        // Використовуємо розраховану ціну з декораторів
        double calculatedPrice = decoratedAd.getCalculatedPrice();

        // Якщо ціна змінилася через декоратори, показуємо це
        if (Math.abs(calculatedPrice - currentAd.getPrice()) > 0.01) {
            priceLabel.setText(String.format("%.2f грн", calculatedPrice));
            priceLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 18px;");
        } else {
            priceLabel.setText(String.format("%.2f грн", calculatedPrice));
        }

        // Основна інформація залишається з оригінального оголошення
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

    private void displayDecoratedInfo() {
        // Отримуємо повну декоровану інформацію
        String decoratedInfo = decoratedAd.getDisplayInfo();

        // Обробляємо текст для кращого відображення
        String processedInfo = processDecoratedText(decoratedInfo);
        decoratedInfoText.setText(processedInfo);

        // Додаємо контейнер до основного макету
        addDecoratedInfoToMainContainer();
    }

    private String processDecoratedText(String originalText) {
        // Замінюємо деякі символи для кращого відображення
        return originalText
                .replace("💰", "💰 ")
                .replace("⭐", "⭐ ")
                .replace("🚨", "🚨 ")
                .replace("🛡️", "🛡️ ")
                .replace("🚚", "🚚 ")
                .replace("✨", "✨ ")
                .replace("🚀", "🚀 ")
                .replace("⚡", "⚡ ")
                .replace("❌", "❌ ")
                .replace("✅", "✅ ")
                .replace("💸", "💸 ")
                .replace("🎯", "🎯 ")
                .replace("📦", "📦 ")
                .replace("📋", "📋 ");
    }

    private void addDecoratedInfoToMainContainer() {
        if (mainContainer != null) {
            // Знаходимо позицію після блоку з описом
            int insertIndex = -1;

            for (int i = 0; i < mainContainer.getChildren().size(); i++) {
                if (mainContainer.getChildren().get(i) instanceof VBox) {
                    VBox vbox = (VBox) mainContainer.getChildren().get(i);
                    // Перевіряємо, чи містить цей VBox наш descriptionText
                    if (containsDescriptionText(vbox)) {
                        insertIndex = i + 1;
                        break;
                    }
                }
            }

            // Якщо не знайшли через описання, шукаємо через мета-інформацію
            if (insertIndex == -1) {
                for (int i = 0; i < mainContainer.getChildren().size(); i++) {
                    if (mainContainer.getChildren().get(i) instanceof VBox) {
                        VBox vbox = (VBox) mainContainer.getChildren().get(i);
                        if (vbox.getStyleClass().contains("detail-section-meta")) {
                            insertIndex = i + 1;
                            break;
                        }
                    }
                }
            }

            // Додаємо декоровану інформацію
            if (insertIndex > 0 && insertIndex <= mainContainer.getChildren().size()) {
                // Перевіряємо, чи вже не додали контейнер
                if (!mainContainer.getChildren().contains(decoratedInfoContainer)) {
                    mainContainer.getChildren().add(insertIndex, decoratedInfoContainer);
                }
            } else {
                // Якщо не знайшли підходящого місця, додаємо перед кнопками
                int buttonBoxIndex = mainContainer.getChildren().indexOf(actionButtonsBox);
                if (buttonBoxIndex > 0) {
                    mainContainer.getChildren().add(buttonBoxIndex, decoratedInfoContainer);
                } else {
                    // В крайньому випадку додаємо в кінець перед останнім елементом
                    mainContainer.getChildren().add(mainContainer.getChildren().size() - 1, decoratedInfoContainer);
                }
            }
        }
    }

    private boolean containsDescriptionText(VBox container) {
        for (var child : container.getChildren()) {
            if (child == descriptionText) {
                return true;
            }
            if (child instanceof VBox || child instanceof HBox) {
                if (containsTextInContainer(child, descriptionText)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsTextInContainer(Object container, Text target) {
        if (container instanceof VBox) {
            VBox vbox = (VBox) container;
            for (var child : vbox.getChildren()) {
                if (child == target) {
                    return true;
                }
                if (child instanceof VBox || child instanceof HBox) {
                    if (containsTextInContainer(child, target)) {
                        return true;
                    }
                }
            }
        } else if (container instanceof HBox) {
            HBox hbox = (HBox) container;
            for (var child : hbox.getChildren()) {
                if (child == target) {
                    return true;
                }
                if (child instanceof VBox || child instanceof HBox) {
                    if (containsTextInContainer(child, target)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Методи для роботи з зображеннями залишаються без змін
    private void loadImages() {
        imagePaths = currentAd.getImagePaths();

        if (imagePaths == null || imagePaths.isEmpty()) {
            showNoImageMessage();
            return;
        }

        loadMainImage(0);
        loadThumbnails();

        if (imagePaths.size() > 1) {
            addGalleryToMainContainer();
        }
    }

    private void showNoImageMessage() {
        adImageView.setVisible(false);
        noImageLabel.setText("Фото не завантажено");
        noImageLabel.setVisible(true);
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

        updateThumbnailStyle(thumbnail, index == currentImageIndex);

        thumbnail.setOnMouseClicked(event -> {
            loadMainImage(index);
            updateAllThumbnailStyles();
        });

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
        if (adImageView.getParent() instanceof VBox) {
            VBox parentContainer = (VBox) adImageView.getParent();
            int imageViewIndex = parentContainer.getChildren().indexOf(adImageView);

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
                editButton.setManaged(true);
            }
            if (isOwner || isAdmin) {
                deleteButton.setVisible(true);
                deleteButton.setManaged(true);
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

    // Utility методи
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

    // Додатковий метод для оновлення декорованої інформації
    public void refreshDecoratedInfo() {
        if (decoratedAd != null && decoratedInfoText != null) {
            String decoratedInfo = decoratedAd.getDisplayInfo();
            String processedInfo = processDecoratedText(decoratedInfo);
            decoratedInfoText.setText(processedInfo);
        }
    }
}
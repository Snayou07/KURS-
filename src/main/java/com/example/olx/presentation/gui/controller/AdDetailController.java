package com.example.olx.presentation.gui.controller;

import com.example.olx.domain.decorator.AdComponent;
import com.example.olx.domain.decorator.AdDecoratorFactory;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.model.User;
import com.example.olx.domain.model.UserType;
import com.example.olx.presentation.gui.MainGuiApp;
import com.example.olx.presentation.gui.util.GlobalContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AdDetailController {

    // --- FXML Поля ---
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
    @FXML private VBox mainContainer;

    // --- Поля класу ---
    private VBox decoratedInfoContainer; // Контейнер для відображення інформації від декораторів
    private VBox decoratedInfoTextContainer; // Контейнер для тексту
    private Ad currentAd;                // Поточне оголошення (оригінальна модель)
    private AdComponent decoratedAd;     // Декорований компонент оголошення

    /**
     * Метод, що викликається JavaFX після завантаження FXML.
     * Ініціалізує деякі UI елементи.
     */
    public void initialize() {
        // Початково ховаємо кнопки редагування та видалення
        if (editButton != null) {
            editButton.setVisible(false);
            editButton.setManaged(false); // Не займає місце в лейауті
        }
        if (deleteButton != null) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false); // Не займає місце в лейауті
        }
        // Налаштовуємо контейнер для відображення декорованої інформації
        setupDecoratedInfoContainer();
    }

    /**
     * Основний метод для ініціалізації контролера даними оголошення.
     * Цей метод отримує оголошення, витягує з нього дані та метадані з мапи,
     * а потім оновлює UI.
     *
     * @param ad Оголошення для відображення.
     */
    public void initData(Ad ad) {
        if (ad == null) {
            showErrorAndGoBack("Не вдалося завантажити деталі оголошення. Об'єкт оголошення відсутній.");
            return;
        }
        this.currentAd = ad;

        // 1. Отримуємо дані просто і чисто, без ручного парсингу рядків
        String cleanDescription = ad.getDescription() != null ? ad.getDescription() : "";
        Map<String, String> metadata = ad.getDecorators();
        if (metadata == null) { // Захист від null, якщо дані прийшли некоректні
            metadata = new HashMap<>();
        }

        // 2. Отримуємо значення з мапи. Це набагато надійніше!
        boolean isPremium = "true".equalsIgnoreCase(metadata.get("premium"));
        boolean isUrgent = "true".equalsIgnoreCase(metadata.get("urgent"));
        Double discountPercentage = parseDouble(metadata.get("discountPercentage"));
        String discountReason = metadata.get("discountReason");
        Integer warrantyMonths = parseInt(metadata.get("warrantyMonths"));
        String warrantyType = metadata.get("warrantyType");
        boolean freeDelivery = "true".equalsIgnoreCase(metadata.get("freeDelivery"));
        Double deliveryCost = parseDouble(metadata.get("deliveryCost"));
        String deliveryInfo = metadata.get("deliveryInfo");

        // 3. Створюємо повністю декорований компонент оголошення
        this.decoratedAd = AdDecoratorFactory.createFullyDecoratedAd(
                ad, isPremium, isUrgent, discountPercentage, discountReason,
                warrantyMonths, warrantyType, freeDelivery, deliveryCost, deliveryInfo
        );

        // 4. Заповнюємо деталі оголошення в UI
        populateAdDetailsUI(cleanDescription);
        setupActionButtons();
        loadAdImages();
        Platform.runLater(this::displayDecoratedAdInfo);
    }

    /**
     * Заповнює UI елементи деталями оголошення.
     * @param displayDescription "Чистий" опис для відображення.
     */
    private void populateAdDetailsUI(String displayDescription) {
        if (decoratedAd == null) {
            showErrorAndGoBack("Помилка ініціалізації декорованого оголошення.");
            return;
        }

        titleLabel.setText(decoratedAd.getFormattedTitle());
        descriptionText.setText(displayDescription);
        priceLabel.setText(String.format("%.2f грн", decoratedAd.getCalculatedPrice()));

        if (Math.abs(decoratedAd.getCalculatedPrice() - currentAd.getPrice()) > 0.01) {
            priceLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            priceLabel.setStyle("");
        }

        adIdLabel.setText("ID: " + currentAd.getAdId());

        try {
            User seller = MainGuiApp.userService.getUserById(currentAd.getSellerId());
            sellerLabel.setText("Продавець: " + (seller != null ? seller.getUsername() : "Невідомий"));
        } catch (Exception e) {
            System.err.println("Помилка отримання продавця: " + e.getMessage());
            sellerLabel.setText("Продавець: Невідомий");
        }

        try {
            Optional<CategoryComponent> categoryOptional = MainGuiApp.categoryService.findCategoryById(currentAd.getCategoryId());
            categoryLabel.setText("Категорія: " + categoryOptional.map(CategoryComponent::getName).orElse("Невідома"));
        } catch (Exception e) {
            System.err.println("Помилка отримання категорії: " + e.getMessage());
            categoryLabel.setText("Категорія: Невідома");
        }
    }

    /**
     * Відображає додаткову інформацію від декораторів (якщо вона є).
     */
    /**
     * Відображає додаткову інформацію від декораторів у зручному для користувача форматі.
     */
    private void displayDecoratedAdInfo() {
        if (decoratedAd == null) return;

        // Очищаємо попередній контент
        decoratedInfoTextContainer.getChildren().clear();

        // Отримуємо метадані декораторів
        Map<String, String> metadata = currentAd.getDecorators();
        if (metadata == null || metadata.isEmpty()) {
            decoratedInfoContainer.setVisible(false);
            decoratedInfoContainer.setManaged(false);
            return;
        }

        // Створюємо зручні мітки замість технічного тексту
        createUserFriendlyDecorators(metadata);

        // Показуємо контейнер якщо є хоча б один декоратор
        if (!decoratedInfoTextContainer.getChildren().isEmpty()) {
            // Додаємо контейнер до головного контейнера, якщо його там немає
            if (!mainContainer.getChildren().contains(decoratedInfoContainer)) {
                int index = mainContainer.getChildren().indexOf(actionButtonsBox);
                if (index != -1) {
                    mainContainer.getChildren().add(index, decoratedInfoContainer);
                } else {
                    mainContainer.getChildren().add(decoratedInfoContainer);
                }
            }
            decoratedInfoContainer.setVisible(true);
            decoratedInfoContainer.setManaged(true);
        } else {
            decoratedInfoContainer.setVisible(false);
            decoratedInfoContainer.setManaged(false);
        }
    }

    /**
     * Створює зручні для користувача мітки декораторів на основі метаданих.
     */
    private void createUserFriendlyDecorators(Map<String, String> metadata) {
        // Преміум товар
        if ("true".equalsIgnoreCase(metadata.get("premium"))) {
            Label premiumLabel = createDecoratorLabel("⭐ Преміум товар", "#f39c12", true);
            decoratedInfoTextContainer.getChildren().add(premiumLabel);
        }

        // Термінова пропозиція
        if ("true".equalsIgnoreCase(metadata.get("urgent"))) {
            Label urgentLabel = createDecoratorLabel("🔥 Термінова пропозиція", "#e74c3c", true);
            urgentLabel.setStyle(urgentLabel.getStyle() + " -fx-effect: dropshadow(gaussian, rgba(231, 76, 60, 0.3), 5, 0, 0, 0);");
            decoratedInfoTextContainer.getChildren().add(urgentLabel);
        }

        // Знижка
        Double discountPercentage = parseDouble(metadata.get("discountPercentage"));
        if (discountPercentage != null && discountPercentage > 0) {
            String discountText = String.format("💰 Знижка %.0f%%", discountPercentage);
            Label discountLabel = createDecoratorLabel(discountText, "#e74c3c", true);
            decoratedInfoTextContainer.getChildren().add(discountLabel);

            // Причина знижки
            String discountReason = metadata.get("discountReason");
            if (discountReason != null && !discountReason.trim().isEmpty()) {
                Label reasonLabel = createDecoratorLabel("💡 " + discountReason, "#7f8c8d", false);
                decoratedInfoTextContainer.getChildren().add(reasonLabel);
            }

            // Показуємо економію
            double originalPrice = currentAd.getPrice();
            double discountAmount = originalPrice * discountPercentage / 100;
            Label savingsLabel = createDecoratorLabel(
                    String.format("💸 Ви економите: %.2f грн", discountAmount),
                    "#27ae60",
                    true
            );
            decoratedInfoTextContainer.getChildren().add(savingsLabel);
        }

        // Гарантія
        Integer warrantyMonths = parseInt(metadata.get("warrantyMonths"));
        String warrantyType = metadata.get("warrantyType");
        if (warrantyMonths != null && warrantyMonths > 0) {
            String warrantyText = String.format("🛡️ Гарантія: %d міс.", warrantyMonths);
            if (warrantyType != null && !warrantyType.trim().isEmpty()) {
                warrantyText += " (" + warrantyType + ")";
            }
            Label warrantyLabel = createDecoratorLabel(warrantyText, "#27ae60", false);
            decoratedInfoTextContainer.getChildren().add(warrantyLabel);
        }

        // Доставка
        boolean freeDelivery = "true".equalsIgnoreCase(metadata.get("freeDelivery"));
        Double deliveryCost = parseDouble(metadata.get("deliveryCost"));
        String deliveryInfo = metadata.get("deliveryInfo");

        if (freeDelivery) {
            Label deliveryLabel = createDecoratorLabel("🚚 Безкоштовна доставка", "#3498db", true);
            decoratedInfoTextContainer.getChildren().add(deliveryLabel);
        } else if (deliveryCost != null && deliveryCost > 0) {
            String deliveryText = String.format("🚚 Доставка: %.2f грн", deliveryCost);
            Label deliveryLabel = createDecoratorLabel(deliveryText, "#3498db", false);
            decoratedInfoTextContainer.getChildren().add(deliveryLabel);
        }

        if (deliveryInfo != null && !deliveryInfo.trim().isEmpty()) {
            Label infoLabel = createDecoratorLabel("📋 " + deliveryInfo, "#7f8c8d", false);
            decoratedInfoTextContainer.getChildren().add(infoLabel);
        }
    }

    /**
     * Створює стилізований Label для декоратора.
     */
    private Label createDecoratorLabel(String text, String color, boolean isBold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(550);

        String style = String.format("-fx-text-fill: %s; -fx-font-size: %dpx;",
                color, isBold ? 13 : 12);
        if (isBold) {
            style += " -fx-font-weight: bold;";
        }

        label.setStyle(style);
        label.setPadding(new Insets(3, 0, 3, 0));

        return label;
    }

    /**
     * Альтернативний метод для створення бейджів замість простого тексту.
     */
    private void createDecoratorBadges(Map<String, String> metadata) {
        HBox badgesContainer = new HBox(8);
        badgesContainer.setPadding(new Insets(0, 0, 10, 0));

        // Преміум бейдж
        if ("true".equalsIgnoreCase(metadata.get("premium"))) {
            Label premiumBadge = createBadge("⭐ Преміум", "#f39c12");
            badgesContainer.getChildren().add(premiumBadge);
        }

        // Термінова пропозиція
        if ("true".equalsIgnoreCase(metadata.get("urgent"))) {
            Label urgentBadge = createBadge("🔥 Терміново", "#e74c3c");
            badgesContainer.getChildren().add(urgentBadge);
        }

        // Знижка
        Double discountPercentage = parseDouble(metadata.get("discountPercentage"));
        if (discountPercentage != null && discountPercentage > 0) {
            Label discountBadge = createBadge(String.format("💰 -%.0f%%", discountPercentage), "#e74c3c");
            badgesContainer.getChildren().add(discountBadge);
        }

        // Безкоштовна доставка
        if ("true".equalsIgnoreCase(metadata.get("freeDelivery"))) {
            Label deliveryBadge = createBadge("🚚 Безкоштовна доставка", "#3498db");
            badgesContainer.getChildren().add(deliveryBadge);
        }

        // Гарантія
        Integer warrantyMonths = parseInt(metadata.get("warrantyMonths"));
        if (warrantyMonths != null && warrantyMonths > 0) {
            Label warrantyBadge = createBadge(String.format("🛡️ Гарантія %d міс.", warrantyMonths), "#27ae60");
            badgesContainer.getChildren().add(warrantyBadge);
        }

        if (!badgesContainer.getChildren().isEmpty()) {
            decoratedInfoTextContainer.getChildren().add(badgesContainer);
        }
    }

    /**
     * Створює стилізований бейдж.
     */
    private Label createBadge(String text, String backgroundColor) {
        Label badge = new Label(text);
        badge.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 4 8 4 8; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold;",
                backgroundColor
        ));
        return badge;
    }

    /**
     * Оновлений метод setupDecoratedInfoContainer з кращим стилем.
     */
    private void setupDecoratedInfoContainer() {
        decoratedInfoContainer = new VBox(10);
        decoratedInfoContainer.setPadding(new Insets(15));
        decoratedInfoContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef); " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );

        Label title = new Label("✨ Спеціальні умови та особливості");
        title.setStyle(
                "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-padding: 0 0 5 0;"
        );

        // Створюємо окремий контейнер для текстового контенту
        decoratedInfoTextContainer = new VBox(8);

        decoratedInfoContainer.getChildren().addAll(title, decoratedInfoTextContainer);
        decoratedInfoContainer.setVisible(false);
        decoratedInfoContainer.setManaged(false);
    }

    // --- Нові, простіші хелпери для безпечного парсингу значень ---
    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            System.err.println("Помилка парсингу Double: " + value);
            return null;
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Помилка парсингу Integer: " + value);
            return null;
        }
    }

    // --- Обробники дій ---
    @FXML
    private void handleBack() {
        try {
            MainGuiApp.loadMainScene();
        } catch (IOException e) {
            System.err.println("Помилка повернення на головну сцену: " + e.getMessage());
            e.printStackTrace();
            showError("Критична помилка", "Не вдалося завантажити головну сторінку.");
        }
    }

    @FXML
    private void handleEditAd() {
        if (currentAd == null) {
            showError("Помилка", "Неможливо редагувати, оголошення не завантажено.");
            return;
        }
        try {
            MainGuiApp.loadEditAdScene(currentAd);
        } catch (IOException e) {
            System.err.println("Помилка завантаження сцени редагування: " + e.getMessage());
            e.printStackTrace();
            showError("Помилка", "Не вдалося відкрити форму редагування.");
        }
    }

    @FXML
    private void handleDeleteAd() {
        if (currentAd == null) {
            showError("Помилка", "Неможливо видалити, оголошення не завантажено.");
            return;
        }
        User currentUser = GlobalContext.getInstance().getLoggedInUser();
        if (currentUser == null) {
            showError("Помилка", "Користувач не авторизований. Неможливо видалити оголошення.");
            return;
        }

        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION,
                "Ви дійсно бажаєте видалити оголошення \"" + currentAd.getTitle() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirmationDialog.setTitle("Підтвердження видалення");
        confirmationDialog.setHeaderText(null);

        confirmationDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    MainGuiApp.adService.deleteAd(currentAd.getAdId(), currentUser.getUserId());
                    showInfoAlert("Успіх", "Оголошення успішно видалено.");
                    handleBack();
                } catch (Exception e) {
                    System.err.println("Помилка видалення оголошення: " + e.getMessage());
                    e.printStackTrace();
                    showError("Помилка видалення", "Не вдалося видалити оголошення. " + e.getMessage());
                }
            }
        });
    }

    /**
     * Налаштовує контейнер для відображення інформації від декораторів.
     */


    /**
     * Налаштовує видимість кнопок "Редагувати" та "Видалити"
     * залежно від прав поточного користувача.
     */
    private void setupActionButtons() {
        User currentUser = GlobalContext.getInstance().getLoggedInUser();
        if (currentUser != null && currentAd != null) {
            boolean isOwner = currentUser.getUserId().equals(currentAd.getSellerId());
            boolean isAdmin = currentUser.getUserType() == UserType.ADMIN;

            editButton.setVisible(isOwner);
            editButton.setManaged(isOwner);

            deleteButton.setVisible(isOwner || isAdmin);
            deleteButton.setManaged(isOwner || isAdmin);
        } else {
            editButton.setVisible(false);
            editButton.setManaged(false);
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }
    }

    /**
     * Завантажує зображення оголошення.
     */
    private void loadAdImages() {
        if (currentAd == null || currentAd.getImagePaths() == null || currentAd.getImagePaths().isEmpty()) {
            noImageLabel.setVisible(true);
            adImageView.setImage(null);
            return;
        }

        noImageLabel.setVisible(false);
        String firstImagePath = currentAd.getImagePaths().get(0);
        loadImageToView(firstImagePath);
    }

    /**
     * Завантажує зображення за вказаним шляхом до ImageView.
     * @param imagePath Шлях до файлу зображення.
     */
    private void loadImageToView(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists() && imageFile.isFile()) {
                Image image = new Image(imageFile.toURI().toString());
                adImageView.setImage(image);
            } else {
                System.err.println("Файл зображення не знайдено або не є файлом: " + imagePath);
                adImageView.setImage(null);
                noImageLabel.setText("Зображення не знайдено");
                noImageLabel.setVisible(true);
            }
        } catch (Exception e) {
            System.err.println("Помилка завантаження зображення '" + imagePath + "': " + e.getMessage());
            e.printStackTrace();
            adImageView.setImage(null);
            noImageLabel.setText("Помилка завантаження зображення");
            noImageLabel.setVisible(true);
        }
    }

    // --- Утилітні методи для відображення повідомлень ---
    private void showErrorAndGoBack(String message) {
        showError("Помилка завантаження", message);
        Platform.runLater(this::handleBack);
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
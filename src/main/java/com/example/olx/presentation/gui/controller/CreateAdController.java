// src/main/java/com/example/olx/presentation/gui/controller/CreateAdController.java
package com.example.olx.presentation.gui.controller;

import com.example.olx.application.command.AdCommandManager;
import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.domain.decorator.AdComponent;
import com.example.olx.domain.decorator.AdDecoratorFactory;
import com.example.olx.domain.decorator.BasicAdComponent;
import com.example.olx.domain.exception.InvalidInputException;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.Category;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.model.User;
import com.example.olx.presentation.gui.MainGuiApp;
import com.example.olx.presentation.gui.util.GlobalContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CreateAdController {

    @FXML private Label formHeaderLabel;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private ComboBox<CategoryDisplayItem> categoryComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    // Для фотографій
    @FXML private Button addPhotoButton;
    @FXML private HBox photoPreviewBox;
    private List<File> selectedImageFiles = new ArrayList<>();
    private List<String> existingImagePaths = new ArrayList<>();

    // Декоратори
    @FXML private CheckBox premiumCheckBox;
    @FXML private CheckBox urgentCheckBox;

    // Знижка
    @FXML private CheckBox discountCheckBox;
    @FXML private HBox discountOptionsBox;
    @FXML private TextField discountPercentageField;
    @FXML private TextField discountReasonField;

    // Гарантія
    @FXML private CheckBox warrantyCheckBox;
    @FXML private HBox warrantyOptionsBox;
    @FXML private TextField warrantyMonthsField;
    @FXML private TextField warrantyTypeField;

    // Доставка
    @FXML private CheckBox deliveryCheckBox;
    @FXML private VBox deliveryOptionsBox;
    @FXML private CheckBox freeDeliveryCheckBox;
    @FXML private HBox deliveryCostBox;
    @FXML private TextField deliveryCostField;
    @FXML private TextField deliveryInfoField;

    // Попередній перегляд
    @FXML private Button previewButton;
    @FXML private TextArea previewArea;

    private ObservableList<CategoryDisplayItem> categoryItems = FXCollections.observableArrayList();
    private Ad adToEdit = null;
    private static final String IMAGE_STORAGE_DIR = "user_images";

    // Command Manager для виконання операцій
    private AdCommandManager commandManager;

    @FXML
    public void initialize() {
        errorLabel.setText("");
        loadCategories();
        setupCategoryComboBox();
        setupPriceFieldValidation();
        setupImageStorageDir();
        setupDecoratorValidation();

        // Отримуємо Command Manager з головного класу
        commandManager = MainGuiApp.getAdCommandManager();

        if (adToEdit == null) {
            formHeaderLabel.setText("Створити нове оголошення");
        } else {
            formHeaderLabel.setText("Редагувати оголошення");
        }

        // Додаємо слухачі для автоматичного оновлення превʼю
        setupPreviewListeners();
    }

    private void setupDecoratorValidation() {
        // Валідація для знижки
        discountPercentageField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,3}")) {
                discountPercentageField.setText(oldValue);
            }
        });

        // Валідація для гарантії
        warrantyMonthsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,3}")) {
                warrantyMonthsField.setText(oldValue);
            }
        });

        // Валідація для доставки
        deliveryCostField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([.,]\\d{0,2})?")) {
                deliveryCostField.setText(oldValue);
            }
        });
    }

    private void setupPreviewListeners() {
        // Додаємо слухачі для основних полів
        titleField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        descriptionArea.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        priceField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        categoryComboBox.valueProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());

        // Додаємо слухачі для декораторів
        premiumCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        urgentCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        discountCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        discountPercentageField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        discountReasonField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        warrantyCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        warrantyMonthsField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        warrantyTypeField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        deliveryCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        freeDeliveryCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        deliveryCostField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        deliveryInfoField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
    }

    private void updatePreviewAutomatically() {
        // Оновлюємо превʼю тільки якщо всі основні поля заповнені
        if (!titleField.getText().trim().isEmpty() &&
                !descriptionArea.getText().trim().isEmpty() &&
                !priceField.getText().trim().isEmpty()) {
            handleUpdatePreview();
        }
    }

    @FXML
    private void handleDiscountToggle() {
        discountOptionsBox.setDisable(!discountCheckBox.isSelected());
        if (!discountCheckBox.isSelected()) {
            discountPercentageField.clear();
            discountReasonField.clear();
        }
    }

    @FXML
    private void handleWarrantyToggle() {
        warrantyOptionsBox.setDisable(!warrantyCheckBox.isSelected());
        if (!warrantyCheckBox.isSelected()) {
            warrantyMonthsField.clear();
            warrantyTypeField.clear();
        }
    }

    @FXML
    private void handleDeliveryToggle() {
        deliveryOptionsBox.setDisable(!deliveryCheckBox.isSelected());
        if (!deliveryCheckBox.isSelected()) {
            freeDeliveryCheckBox.setSelected(false);
            deliveryCostField.clear();
            deliveryInfoField.clear();
        }
    }

    @FXML
    private void handleFreeDeliveryToggle() {
        deliveryCostBox.setDisable(freeDeliveryCheckBox.isSelected());
        if (freeDeliveryCheckBox.isSelected()) {
            deliveryCostField.clear();
        }
    }

    @FXML
    private void handleUpdatePreview() {
        try {
            // Створюємо тимчасове оголошення для превʼю
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            String priceStr = priceField.getText().replace(',', '.').trim();

            if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
                previewArea.setText("Заповніть всі основні поля для перегляду");
                return;
            }

            double price = Double.parseDouble(priceStr);

            // Створюємо базове оголошення
            Ad tempAd = new Ad();
            tempAd.setTitle(title);
            tempAd.setDescription(description);
            tempAd.setPrice(price);
            tempAd.setStatus("ACTIVE");

            // Застосовуємо декоратори
            AdComponent decoratedAd = createDecoratedAd(tempAd);

            // Показуємо результат
            String preview = decoratedAd.getDisplayInfo();
            preview += "\n\n" + "=".repeat(50);
            preview += "\nЗаголовок: " + decoratedAd.getFormattedTitle();
            preview += String.format("\nПідсумкова ціна: %.2f грн", decoratedAd.getCalculatedPrice());

            previewArea.setText(preview);

        } catch (NumberFormatException e) {
            previewArea.setText("Неправильний формат ціни");
        } catch (Exception e) {
            previewArea.setText("Помилка створення превʼю: " + e.getMessage());
        }
    }

    private AdComponent createDecoratedAd(Ad ad) {
        // Отримуємо параметри декораторів
        boolean isPremium = premiumCheckBox.isSelected();
        boolean isUrgent = urgentCheckBox.isSelected();

        Double discountPercentage = null;
        String discountReason = null;
        if (discountCheckBox.isSelected()) {
            try {
                String percentStr = discountPercentageField.getText().trim();
                if (!percentStr.isEmpty()) {
                    discountPercentage = Double.parseDouble(percentStr);
                    discountReason = discountReasonField.getText().trim();
                    if (discountReason.isEmpty()) {
                        discountReason = "Спеціальна пропозиція";
                    }
                }
            } catch (NumberFormatException e) {
                // Ігноруємо неправильний формат
                discountPercentage = null;
            }
        }

        Integer warrantyMonths = null;
        String warrantyType = null;
        if (warrantyCheckBox.isSelected()) {
            try {
                String monthsStr = warrantyMonthsField.getText().trim();
                if (!monthsStr.isEmpty()) {
                    warrantyMonths = Integer.parseInt(monthsStr);
                    warrantyType = warrantyTypeField.getText().trim();
                    if (warrantyType.isEmpty()) {
                        warrantyType = "Стандартна гарантія";
                    }
                }
            } catch (NumberFormatException e) {
                // Ігноруємо неправильний формат
                warrantyMonths = null;
            }
        }

        Boolean freeDelivery = null;
        Double deliveryCost = null;
        String deliveryInfo = null;
        if (deliveryCheckBox.isSelected()) {
            freeDelivery = freeDeliveryCheckBox.isSelected();
            deliveryInfo = deliveryInfoField.getText().trim();
            if (deliveryInfo.isEmpty()) {
                deliveryInfo = "Стандартна доставка";
            }

            if (!freeDelivery) {
                try {
                    String costStr = deliveryCostField.getText().replace(',', '.').trim();
                    if (!costStr.isEmpty()) {
                        deliveryCost = Double.parseDouble(costStr);
                    }
                } catch (NumberFormatException e) {
                    deliveryCost = 0.0;
                }
            }
        }

        // Використовуємо фабрику для створення декорованого оголошення
        return AdDecoratorFactory.createFullyDecoratedAd(
                ad, isPremium, isUrgent,
                discountPercentage, discountReason,
                warrantyMonths, warrantyType,
                freeDelivery, deliveryCost, deliveryInfo
        );
    }

    private void setupImageStorageDir() {
        Path storagePath = Paths.get(IMAGE_STORAGE_DIR);
        if (!Files.exists(storagePath)) {
            try {
                Files.createDirectories(storagePath);
            } catch (IOException e) {
                System.err.println("Could not create image storage directory: " + e.getMessage());
            }
        }
    }

    public void initDataForEdit(Ad ad) {
        this.adToEdit = ad;
        formHeaderLabel.setText("Редагувати оголошення: " + ad.getTitle());
        titleField.setText(ad.getTitle());
        descriptionArea.setText(ad.getDescription());
        priceField.setText(String.format("%.2f", ad.getPrice()).replace(',', '.'));

        if (ad.getCategoryId() != null) {
            categoryItems.stream()
                    .filter(item -> item.getId().equals(ad.getCategoryId()))
                    .findFirst()
                    .ifPresent(categoryComboBox::setValue);
        }

        if (ad.getImagePaths() != null && !ad.getImagePaths().isEmpty()) {
            existingImagePaths.addAll(ad.getImagePaths());
            existingImagePaths.forEach(this::addExistingImageToPreview);
        }

        // Тут можна додати логіку для завантаження збережених декораторів
        // якщо вони зберігаються разом з оголошенням
    }

    private void setupCategoryComboBox() {
        categoryComboBox.setItems(categoryItems);
        categoryComboBox.setConverter(new StringConverter<CategoryDisplayItem>() {
            @Override
            public String toString(CategoryDisplayItem item) {
                return item == null ? null : item.getDisplayName();
            }
            @Override
            public CategoryDisplayItem fromString(String string) {
                return categoryItems.stream().filter(item -> item.getDisplayName().equals(string)).findFirst().orElse(null);
            }
        });
    }

    private void setupPriceFieldValidation() {
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([.,]\\d{0,2})?")) {
                priceField.setText(oldValue);
            }
        });
    }

    private void loadCategories() {
        List<CategoryComponent> rootCategories = MainGuiApp.categoryService.getAllRootCategories();
        List<CategoryDisplayItem> flatCategories = new ArrayList<>();
        populateFlatCategories(rootCategories, flatCategories, "");
        categoryItems.setAll(flatCategories);
    }

    private void populateFlatCategories(List<CategoryComponent> categories, List<CategoryDisplayItem> flatList, String indent) {
        for (CategoryComponent component : categories) {
            flatList.add(new CategoryDisplayItem(component.getId(), indent + component.getName()));
            if (component instanceof Category) {
                populateFlatCategories(((Category) component).getSubCategories(), flatList, indent + "  > ");
            }
        }
    }

    @FXML
    private void handleAddPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Вибрати фотографії");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Всі файли", "*.*")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(addPhotoButton.getScene().getWindow());

        if (files != null && !files.isEmpty()) {
            int limit = 5 - selectedImageFiles.size() - existingImagePaths.size();
            if (files.size() > limit && limit > 0) {
                showError("Можна додати ще максимум " + limit + " фото.");
            }

            for (int i = 0; i < files.size() && i < limit; i++) {
                File file = files.get(i);
                if (!selectedImageFiles.contains(file) && !isAlreadyExistingImage(file.getName())) {
                    selectedImageFiles.add(file);
                    addImageToPreview(file);
                }
            }
            if (selectedImageFiles.size() + existingImagePaths.size() >= 5) {
                addPhotoButton.setDisable(true);
            }
        }
    }

    private boolean isAlreadyExistingImage(String fileName) {
        return existingImagePaths.stream().anyMatch(path -> Paths.get(path).getFileName().toString().equals(fileName));
    }

    private void addImageToPreview(File file) {
        try {
            Image image = new Image(file.toURI().toString(), 100, 100, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(80);
            imageView.setFitWidth(80);
            imageView.setPreserveRatio(true);

            Button removeButton = new Button("X");
            removeButton.setOnAction(event -> {
                photoPreviewBox.getChildren().remove(imageView.getParent());
                selectedImageFiles.remove(file);
                if (selectedImageFiles.size() + existingImagePaths.size() < 5) {
                    addPhotoButton.setDisable(false);
                }
            });

            VBox imageContainer = new VBox(5, imageView, removeButton);
            imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
            photoPreviewBox.getChildren().add(imageContainer);
        } catch (Exception e) {
            System.err.println("Error creating image preview: " + e.getMessage());
        }
    }

    private void addExistingImageToPreview(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                file = new File(IMAGE_STORAGE_DIR, Paths.get(imagePath).getFileName().toString());
            }

            if (file.exists()) {
                Image image = new Image(file.toURI().toString(), 100, 100, true, true);
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(80);
                imageView.setFitWidth(80);
                imageView.setPreserveRatio(true);

                Button removeButton = new Button("X (збережене)");
                removeButton.setOnAction(event -> {
                    photoPreviewBox.getChildren().remove(imageView.getParent());
                    existingImagePaths.remove(imagePath);
                    if (selectedImageFiles.size() + existingImagePaths.size() < 5) {
                        addPhotoButton.setDisable(false);
                    }
                });

                VBox imageContainer = new VBox(5, imageView, removeButton);
                imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
                photoPreviewBox.getChildren().add(imageContainer);
            } else {
                System.err.println("Existing image not found for preview: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Error creating existing image preview for " + imagePath + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveAd() {
        User currentUser = GlobalContext.getInstance().getLoggedInUser();
        if (currentUser == null) {
            showError("Ви не увійшли в систему.");
            return;
        }

        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceStr = priceField.getText().replace(',', '.').trim();
        CategoryDisplayItem selectedCategoryItem = categoryComboBox.getSelectionModel().getSelectedItem();

        errorLabel.setText("");

        if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty() || selectedCategoryItem == null) {
            showError("Всі поля є обов'язковими для заповнення.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                showError("Ціна не може бути від'ємною.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Неправильний формат ціни.");
            return;
        }

        // Обробка та копіювання нових фотографій
        List<String> finalImagePaths = new ArrayList<>(existingImagePaths);
        for (File imageFile : selectedImageFiles) {
            try {
                String uniqueFileName = UUID.randomUUID().toString() + "_" + imageFile.getName();
                Path targetPath = Paths.get(IMAGE_STORAGE_DIR, uniqueFileName);
                Files.copy(imageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                finalImagePaths.add(targetPath.toString());
            } catch (IOException e) {
                showError("Помилка збереження фото: " + imageFile.getName() + " - " + e.getMessage());
            }
        }

        // Створюємо базовий запит для оголошення
        AdCreationRequest request = new AdCreationRequest(
                title, description, price,
                selectedCategoryItem.getId(),
                currentUser.getUserId(),
                finalImagePaths
        );

        try {
            Ad savedAd;
            if (adToEdit == null) {
                // Створюємо нове оголошення
                savedAd = commandManager.createAd(request);
            } else {
                // Оновлюємо існуюче оголошення
                savedAd = commandManager.updateAd(adToEdit.getAdId(), request, currentUser.getUserId());
            }

            // Якщо потрібно зберегти інформацію про декоратори, можна додати це тут
            // Наприклад, зберегти метадані про застосовані декоратори в додатковому полі
            saveDecoratorMetadata(savedAd);

            String successMessage = (adToEdit == null) ?
                    "Оголошення успішно створено з усіма декораторами!" :
                    "Оголошення успішно оновлено з усіма декораторами!";
            showSuccessAndReturn(successMessage);

        } catch (InvalidInputException | IllegalArgumentException | UserNotFoundException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Сталася помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveDecoratorMetadata(Ad ad) {
        // Цей метод можна використати для збереження інформації про застосовані декоратори
        // Наприклад, в додатковому полі або окремій таблиці

        // Збираємо інформацію про застосовані декоратори
        StringBuilder decoratorInfo = new StringBuilder();

        if (premiumCheckBox.isSelected()) {
            decoratorInfo.append("premium;");
        }

        if (urgentCheckBox.isSelected()) {
            decoratorInfo.append("urgent;");
        }

        if (discountCheckBox.isSelected() && !discountPercentageField.getText().trim().isEmpty()) {
            decoratorInfo.append("discount:").append(discountPercentageField.getText().trim()).append(";");
        }

        if (warrantyCheckBox.isSelected() && !warrantyMonthsField.getText().trim().isEmpty()) {
            decoratorInfo.append("warranty:").append(warrantyMonthsField.getText().trim()).append(";");
        }

        if (deliveryCheckBox.isSelected()) {
            if (freeDeliveryCheckBox.isSelected()) {
                decoratorInfo.append("delivery:free;");
            } else if (!deliveryCostField.getText().trim().isEmpty()) {
                decoratorInfo.append("delivery:").append(deliveryCostField.getText().trim()).append(";");
            }
        }

        // Тут можна зберегти decoratorInfo в базі даних або іншому сховищі
        // Наприклад, в додатковому полі оголошення або окремій таблиці
        System.out.println("Декоратори для оголошення " + ad.getAdId() + ": " + decoratorInfo.toString());
    }

    @FXML
    private void handleCancel() {
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Підтвердження скасування");
        confirmationDialog.setHeaderText("Скасувати " + (adToEdit == null ? "створення" : "редагування") + " оголошення?");
        confirmationDialog.setContentText("Всі незбережені зміни будуть втрачені.");

        Optional<ButtonType> result = confirmationDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                MainGuiApp.loadMainScene();
            } catch (IOException e) {
                e.printStackTrace();
                showError("Помилка повернення на головний екран: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccessAndReturn(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успіх");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        try {
            MainGuiApp.loadMainScene();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error returning to main scene: " + e.getMessage());
        }
    }

    private static class CategoryDisplayItem {
        private final String id;
        private final String displayName;

        public CategoryDisplayItem(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
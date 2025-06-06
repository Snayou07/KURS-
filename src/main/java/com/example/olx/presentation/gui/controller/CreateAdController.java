package com.example.olx.presentation.gui.controller;

import com.example.olx.application.command.AdCommandManager;
import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.domain.decorator.AdComponent;
import com.example.olx.domain.decorator.AdDecoratorFactory;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class CreateAdController {

    // --- FXML Fields ---
    @FXML private Label formHeaderLabel;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private ComboBox<CategoryDisplayItem> categoryComboBox;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    @FXML private Label errorLabel;
    @FXML private Button addPhotoButton;
    @FXML private HBox photoPreviewBox;
    @FXML private CheckBox premiumCheckBox;
    @FXML private CheckBox urgentCheckBox;
    @FXML private CheckBox discountCheckBox;
    @FXML private HBox discountOptionsBox;
    @FXML private TextField discountPercentageField;
    @FXML private TextField discountReasonField;
    @FXML private CheckBox warrantyCheckBox;
    @FXML private HBox warrantyOptionsBox;
    @FXML private TextField warrantyMonthsField;
    @FXML private TextField warrantyTypeField;
    @FXML private CheckBox deliveryCheckBox;
    @FXML private VBox deliveryOptionsBox;
    @FXML private CheckBox freeDeliveryCheckBox;
    @FXML private HBox deliveryCostBox;
    @FXML private TextField deliveryCostField;
    @FXML private TextField deliveryInfoField;
    @FXML private Button previewButton;
    @FXML private TextArea previewArea;

    // --- Class Fields ---
    private ObservableList<CategoryDisplayItem> categoryItems = FXCollections.observableArrayList();
    private Ad adToEdit = null;
    private static final String IMAGE_STORAGE_DIR = "user_images";
    private AdCommandManager commandManager;
    private List<File> selectedImageFiles = new ArrayList<>();
    private List<String> existingImagePaths = new ArrayList<>();
    private static final String METADATA_SEPARATOR = "\n\n---DECORATORS---\n";

    @FXML
    public void initialize() {
        errorLabel.setText("");
        try {
            loadCategories();
            setupCategoryComboBox();
            setupPriceFieldValidation();
            setupImageStorageDir();
            setupDecoratorValidation();
            setupPreviewListeners();

            commandManager = MainGuiApp.getAdCommandManager();
            if (commandManager == null) {
                showError("Помилка ініціалізації системи.");
                return;
            }

            if (formHeaderLabel != null) {
                formHeaderLabel.setText(adToEdit == null ? "Створити нове оголошення" : "Редагувати оголошення");
            }
            updatePreviewAutomatically();
        } catch (Exception e) {
            showError("Помилка ініціалізації: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        try {
            // Повертаємося на головну сторінку замість неіснуючої user-ads-view.fxml
            MainGuiApp.loadMainScene();
        } catch (IOException e) {
            System.err.println("Помилка при поверненні на головну сторінку: " + e.getMessage());
            e.printStackTrace();
            // Альтернативний спосіб - просто закрити вікно
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        }
    }

    // --- ГОТОВИЙ МЕТОД ДЛЯ НАВІГАЦІЇ ---
    private void showSuccessAndReturn(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успіх");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait().ifPresent(response -> {
            try {
                // Повертаємося на головну сторінку замість неіснуючої user-ads-view.fxml
                MainGuiApp.loadMainScene();
            } catch (IOException e) {
                System.err.println("Помилка при поверненні на головну сторінку: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void initDataForEdit(Ad ad) {
        this.adToEdit = ad;
        if (formHeaderLabel != null) {
            formHeaderLabel.setText("Редагувати оголошення: " + ad.getTitle());
        }
        titleField.setText(ad.getTitle());
        priceField.setText(String.format("%.2f", ad.getPrice()).replace(',', '.'));

        String fullDescription = ad.getDescription() != null ? ad.getDescription() : "";
        int separatorIndex = fullDescription.indexOf(METADATA_SEPARATOR);
        if (separatorIndex != -1) {
            descriptionArea.setText(fullDescription.substring(0, separatorIndex));
            String metadata = fullDescription.substring(separatorIndex + METADATA_SEPARATOR.length());
            parseAndApplyDecoratorMetadata(metadata);
        } else {
            descriptionArea.setText(fullDescription);
        }

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
        updatePreviewAutomatically();
    }

    @FXML
    private void handleSaveAd() {
        try {
            User currentUser = GlobalContext.getInstance().getLoggedInUser();
            if (currentUser == null) {
                showError("Ви не увійшли в систему.");
                return;
            }

            if (titleField.getText().trim().isEmpty() ||
                    descriptionArea.getText().trim().isEmpty() ||
                    priceField.getText().trim().isEmpty() ||
                    categoryComboBox.getSelectionModel().getSelectedItem() == null) {
                showError("Всі поля обов'язкові для заповнення.");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceField.getText().replace(',', '.').trim());
                if (price < 0) {
                    showError("Ціна не може бути від'ємною.");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Введіть коректну ціну.");
                return;
            }

            List<String> finalImagePaths = new ArrayList<>(existingImagePaths);
            for (File imageFile : selectedImageFiles) {
                String uniqueFileName = UUID.randomUUID().toString() + "_" + imageFile.getName();
                Path targetPath = Paths.get(IMAGE_STORAGE_DIR, uniqueFileName);
                Files.copy(imageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                finalImagePaths.add(targetPath.toString());
            }

            String cleanDescription = descriptionArea.getText().trim();
            String decoratorMetadata = buildDecoratorMetadata();
            String fullDescription = cleanDescription + METADATA_SEPARATOR + decoratorMetadata;

            AdCreationRequest request = new AdCreationRequest(
                    titleField.getText().trim(),
                    fullDescription,
                    price,
                    categoryComboBox.getSelectionModel().getSelectedItem().getId(),
                    currentUser.getUserId(),
                    finalImagePaths
            );

            if (adToEdit == null) {
                commandManager.createAd(request);
                showSuccessAndReturn("Оголошення успішно створено!");
            } else {
                commandManager.updateAd(adToEdit.getAdId(), request, currentUser.getUserId());
                showSuccessAndReturn("Оголошення успішно оновлено!");
            }

        } catch (Exception e) {
            showError("Сталася помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseAndApplyDecoratorMetadata(String metadata) {
        if (metadata == null || metadata.isEmpty()) return;
        Stream.of(metadata.split(";")).forEach(pair -> {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length < 2) return;
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            switch (key) {
                case "premium":
                    if (premiumCheckBox != null) premiumCheckBox.setSelected(Boolean.parseBoolean(value));
                    break;
                case "urgent":
                    if (urgentCheckBox != null) urgentCheckBox.setSelected(Boolean.parseBoolean(value));
                    break;
                case "discount":
                    if (discountCheckBox != null) {
                        discountCheckBox.setSelected(Boolean.parseBoolean(value));
                        handleDiscountToggle();
                    }
                    break;
                case "discountPercentage":
                    if (discountPercentageField != null) discountPercentageField.setText(value);
                    break;
                case "discountReason":
                    if (discountReasonField != null) discountReasonField.setText(value);
                    break;
                case "warranty":
                    if (warrantyCheckBox != null) {
                        warrantyCheckBox.setSelected(Boolean.parseBoolean(value));
                        handleWarrantyToggle();
                    }
                    break;
                case "warrantyMonths":
                    if (warrantyMonthsField != null) warrantyMonthsField.setText(value);
                    break;
                case "warrantyType":
                    if (warrantyTypeField != null) warrantyTypeField.setText(value);
                    break;
                case "delivery":
                    if (deliveryCheckBox != null) {
                        deliveryCheckBox.setSelected(Boolean.parseBoolean(value));
                        handleDeliveryToggle();
                    }
                    break;
                case "freeDelivery":
                    if (freeDeliveryCheckBox != null) {
                        freeDeliveryCheckBox.setSelected(Boolean.parseBoolean(value));
                        handleFreeDeliveryToggle();
                    }
                    break;
                case "deliveryCost":
                    if (deliveryCostField != null) deliveryCostField.setText(value);
                    break;
                case "deliveryInfo":
                    if (deliveryInfoField != null) deliveryInfoField.setText(value);
                    break;
            }
        });
    }

    private String buildDecoratorMetadata() {
        StringBuilder sb = new StringBuilder();
        sb.append("premium:").append(premiumCheckBox != null ? premiumCheckBox.isSelected() : false).append(";");
        sb.append("urgent:").append(urgentCheckBox != null ? urgentCheckBox.isSelected() : false).append(";");
        sb.append("discount:").append(discountCheckBox != null ? discountCheckBox.isSelected() : false).append(";");
        if (discountCheckBox != null && discountCheckBox.isSelected()) {
            sb.append("discountPercentage:").append(discountPercentageField != null ? discountPercentageField.getText() : "").append(";");
            sb.append("discountReason:").append(discountReasonField != null ? discountReasonField.getText() : "").append(";");
        }
        sb.append("warranty:").append(warrantyCheckBox != null ? warrantyCheckBox.isSelected() : false).append(";");
        if (warrantyCheckBox != null && warrantyCheckBox.isSelected()) {
            sb.append("warrantyMonths:").append(warrantyMonthsField != null ? warrantyMonthsField.getText() : "").append(";");
            sb.append("warrantyType:").append(warrantyTypeField != null ? warrantyTypeField.getText() : "").append(";");
        }
        sb.append("delivery:").append(deliveryCheckBox != null ? deliveryCheckBox.isSelected() : false).append(";");
        if (deliveryCheckBox != null && deliveryCheckBox.isSelected()) {
            sb.append("freeDelivery:").append(freeDeliveryCheckBox != null ? freeDeliveryCheckBox.isSelected() : false).append(";");
            sb.append("deliveryCost:").append(deliveryCostField != null ? deliveryCostField.getText() : "").append(";");
            sb.append("deliveryInfo:").append(deliveryInfoField != null ? deliveryInfoField.getText() : "").append(";");
        }
        return sb.toString();
    }

    @FXML
    private void handleUpdatePreview() {
        if (previewArea == null) return;
        try {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            String priceStr = priceField.getText().replace(',', '.').trim();
            if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
                previewArea.setText("Заповніть всі основні поля для попереднього перегляду");
                return;
            }
            Ad tempAd = new Ad();
            tempAd.setTitle(title);
            tempAd.setDescription(description);
            tempAd.setPrice(Double.parseDouble(priceStr));
            tempAd.setStatus("ACTIVE");
            AdComponent decoratedAd = createDecoratedAd(tempAd);
            String preview = decoratedAd.getDisplayInfo() + "\n\n" + "=".repeat(50) +
                    "\nЗаголовок: " + decoratedAd.getFormattedTitle() +
                    String.format("\nІтогова ціна: %.2f грн", decoratedAd.getCalculatedPrice());
            previewArea.setText(preview);
        } catch (Exception e) {
            previewArea.setText("Помилка створення попереднього перегляду: " + e.getMessage());
        }
    }

    private void updatePreviewAutomatically() {
        handleUpdatePreview();
    }

    @FXML
    private void handleDiscountToggle() {
        if (discountCheckBox == null || discountOptionsBox == null) return;
        boolean selected = discountCheckBox.isSelected();
        discountOptionsBox.setVisible(selected);
        discountOptionsBox.setManaged(selected);
        if (!selected) {
            if (discountPercentageField != null) discountPercentageField.clear();
            if (discountReasonField != null) discountReasonField.clear();
        }
        updatePreviewAutomatically();
    }

    @FXML
    private void handleWarrantyToggle() {
        if (warrantyCheckBox == null || warrantyOptionsBox == null) return;
        boolean selected = warrantyCheckBox.isSelected();
        warrantyOptionsBox.setVisible(selected);
        warrantyOptionsBox.setManaged(selected);
        if (!selected) {
            if (warrantyMonthsField != null) warrantyMonthsField.clear();
            if (warrantyTypeField != null) warrantyTypeField.clear();
        }
        updatePreviewAutomatically();
    }

    @FXML
    private void handleDeliveryToggle() {
        if (deliveryCheckBox == null || deliveryOptionsBox == null) return;
        boolean selected = deliveryCheckBox.isSelected();
        deliveryOptionsBox.setVisible(selected);
        deliveryOptionsBox.setManaged(selected);
        if (!selected) {
            if (freeDeliveryCheckBox != null) freeDeliveryCheckBox.setSelected(false);
            if (deliveryCostField != null) deliveryCostField.clear();
            if (deliveryInfoField != null) deliveryInfoField.clear();
        }
        updatePreviewAutomatically();
    }

    @FXML
    private void handleFreeDeliveryToggle() {
        if (freeDeliveryCheckBox == null || deliveryCostBox == null) return;
        boolean selected = freeDeliveryCheckBox.isSelected();
        deliveryCostBox.setVisible(!selected);
        deliveryCostBox.setManaged(!selected);
        if (selected && deliveryCostField != null) {
            deliveryCostField.clear();
        }
        updatePreviewAutomatically();
    }

    private void setupDecoratorValidation() {
        if (discountOptionsBox != null) {
            discountOptionsBox.setVisible(false);
            discountOptionsBox.setManaged(false);
        }
        if (warrantyOptionsBox != null) {
            warrantyOptionsBox.setVisible(false);
            warrantyOptionsBox.setManaged(false);
        }
        if (deliveryOptionsBox != null) {
            deliveryOptionsBox.setVisible(false);
            deliveryOptionsBox.setManaged(false);
        }
        if (discountCheckBox != null) discountCheckBox.setOnAction(event -> handleDiscountToggle());
        if (warrantyCheckBox != null) warrantyCheckBox.setOnAction(event -> handleWarrantyToggle());
        if (deliveryCheckBox != null) deliveryCheckBox.setOnAction(event -> handleDeliveryToggle());
        if (freeDeliveryCheckBox != null) freeDeliveryCheckBox.setOnAction(event -> handleFreeDeliveryToggle());
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        } else {
            new Alert(Alert.AlertType.ERROR, message).showAndWait();
        }
    }

    private AdComponent createDecoratedAd(Ad ad) {
        Double discountPercentage = null;
        String discountReason = null;
        if (discountCheckBox != null && discountCheckBox.isSelected()) {
            try {
                if (discountPercentageField != null && !discountPercentageField.getText().trim().isEmpty()) {
                    discountPercentage = Double.parseDouble(discountPercentageField.getText().trim());
                }
                if (discountReasonField != null) {
                    discountReason = discountReasonField.getText().trim();
                }
            } catch (Exception e) { /* ignore */ }
        }

        Integer warrantyMonths = null;
        String warrantyType = null;
        if (warrantyCheckBox != null && warrantyCheckBox.isSelected()) {
            try {
                if (warrantyMonthsField != null && !warrantyMonthsField.getText().trim().isEmpty()) {
                    warrantyMonths = Integer.parseInt(warrantyMonthsField.getText().trim());
                }
                if (warrantyTypeField != null) {
                    warrantyType = warrantyTypeField.getText().trim();
                }
            } catch (Exception e) { /* ignore */ }
        }

        Boolean freeDelivery = null;
        Double deliveryCost = null;
        String deliveryInfo = null;
        if (deliveryCheckBox != null && deliveryCheckBox.isSelected()) {
            freeDelivery = freeDeliveryCheckBox != null ? freeDeliveryCheckBox.isSelected() : false;
            if (deliveryInfoField != null) {
                deliveryInfo = deliveryInfoField.getText().trim();
            }
            if (!freeDelivery && deliveryCostField != null && !deliveryCostField.getText().trim().isEmpty()) {
                try {
                    deliveryCost = Double.parseDouble(deliveryCostField.getText().trim());
                } catch (Exception e) { /* ignore */ }
            }
        }

        return AdDecoratorFactory.createFullyDecoratedAd(
                ad,
                premiumCheckBox != null ? premiumCheckBox.isSelected() : false,
                urgentCheckBox != null ? urgentCheckBox.isSelected() : false,
                discountPercentage,
                discountReason,
                warrantyMonths,
                warrantyType,
                freeDelivery,
                deliveryCost,
                deliveryInfo
        );
    }

    private void setupPreviewListeners() {
        if (titleField != null) titleField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (descriptionArea != null) descriptionArea.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (priceField != null) priceField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (categoryComboBox != null) categoryComboBox.valueProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (premiumCheckBox != null) premiumCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (urgentCheckBox != null) urgentCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (discountCheckBox != null) discountCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (discountPercentageField != null) discountPercentageField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (discountReasonField != null) discountReasonField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (warrantyCheckBox != null) warrantyCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (warrantyMonthsField != null) warrantyMonthsField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (warrantyTypeField != null) warrantyTypeField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (deliveryCheckBox != null) deliveryCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (freeDeliveryCheckBox != null) freeDeliveryCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (deliveryCostField != null) deliveryCostField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        if (deliveryInfoField != null) deliveryInfoField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
    }

    private void setupImageStorageDir() {
        try {
            Files.createDirectories(Paths.get(IMAGE_STORAGE_DIR));
        } catch (IOException e) {
            System.err.println("Не вдалося створити директорію для зображень: " + e.getMessage());
        }
    }

    private void setupCategoryComboBox() {
        if (categoryComboBox != null) {
            categoryComboBox.setItems(categoryItems);
            categoryComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(CategoryDisplayItem item) {
                    return item == null ? null : item.getDisplayName();
                }
                @Override
                public CategoryDisplayItem fromString(String string) {
                    return categoryItems.stream()
                            .filter(i -> i.getDisplayName().equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
        }
    }

    private void setupPriceFieldValidation() {
        if (priceField != null) {
            priceField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*([.,]\\d{0,2})?")) {
                    priceField.setText(oldValue);
                }
            });
        }
    }

    private void loadCategories() {
        if (MainGuiApp.categoryService == null) {
            showError("Помилка завантаження категорій.");
            return;
        }
        List<CategoryDisplayItem> flatCategories = new ArrayList<>();
        populateFlatCategories(MainGuiApp.categoryService.getAllRootCategories(), flatCategories, "");
        categoryItems.setAll(flatCategories);
    }

    private void populateFlatCategories(List<CategoryComponent> categories, List<CategoryDisplayItem> flatList, String indent) {
        if (categories == null) return;
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
        fileChooser.setTitle("Вибрати фото");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg"));
        List<File> files = fileChooser.showOpenMultipleDialog(addPhotoButton.getScene().getWindow());
        if (files != null) {
            int limit = 5 - selectedImageFiles.size() - existingImagePaths.size();
            if (files.size() > limit) {
                showError("Можна додати ще максимум " + limit + " фото.");
            }
            for (int i = 0; i < files.size() && i < limit; i++) {
                File file = files.get(i);
                if (!selectedImageFiles.contains(file)) {
                    selectedImageFiles.add(file);
                    addImageToPreview(file);
                }
            }
            if (selectedImageFiles.size() + existingImagePaths.size() >= 5 && addPhotoButton != null) {
                addPhotoButton.setDisable(true);
            }
        }
    }

    private void addImageToPreview(File file) {
        try {
            ImageView imageView = new ImageView(new Image(file.toURI().toString(), 100, 100, true, true));
            Button removeButton = new Button("X");
            VBox imageContainer = new VBox(5, imageView, removeButton);
            removeButton.setOnAction(event -> {
                if (photoPreviewBox != null) {
                    photoPreviewBox.getChildren().remove(imageContainer);
                }
                selectedImageFiles.remove(file);
                if (addPhotoButton != null) {
                    addPhotoButton.setDisable(false);
                }
            });
            if (photoPreviewBox != null) {
                photoPreviewBox.getChildren().add(imageContainer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addExistingImageToPreview(String imagePath) {
        try {
            File file = new File(imagePath);
            ImageView imageView = new ImageView(new Image(file.toURI().toString(), 100, 100, true, true));
            Button removeButton = new Button("X");
            VBox imageContainer = new VBox(5, imageView, removeButton);
            removeButton.setOnAction(event -> {
                if (photoPreviewBox != null) {
                    photoPreviewBox.getChildren().remove(imageContainer);
                }
                existingImagePaths.remove(imagePath);
                if (addPhotoButton != null) {
                    addPhotoButton.setDisable(false);
                }
            });
            if (photoPreviewBox != null) {
                photoPreviewBox.getChildren().add(imageContainer);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
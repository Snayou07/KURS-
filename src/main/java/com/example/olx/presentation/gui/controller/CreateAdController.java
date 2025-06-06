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
                showError("Ошибка инициализации системы.");
                return;
            }

            if (formHeaderLabel != null) {
                formHeaderLabel.setText(adToEdit == null ? "Создать новое объявление" : "Редактировать объявление");
            }
            updatePreviewAutomatically();
        } catch (Exception e) {
            showError("Ошибка инициализации: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleCancel() {
        // Отримуємо Stage (вікно) з кнопки
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        // Закриваємо вікно
        stage.close();
    }

    // --- ГОТОВЫЙ МЕТОД ДЛЯ НАВИГАЦИИ ---
    private void showSuccessAndReturn(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait().ifPresent(response -> {
            if (saveButton != null && saveButton.getScene() != null) {
                // Вызываем метод из MainGuiApp для смены окна
                // УБЕДИТЕСЬ, ЧТО ИМЯ FXML ФАЙЛА ВЕРНОЕ
                MainGuiApp.loadScene("user-ads-view.fxml");
            }
        });
    }

    // --- Остальной код класса остается без изменений ---

    public void initDataForEdit(Ad ad) {
        this.adToEdit = ad;
        formHeaderLabel.setText("Редактировать объявление: " + ad.getTitle());
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
                showError("Вы не вошли в систему.");
                return;
            }

            if (titleField.getText().trim().isEmpty() || descriptionArea.getText().trim().isEmpty() || priceField.getText().trim().isEmpty() || categoryComboBox.getSelectionModel().getSelectedItem() == null) {
                showError("Все поля обязательны для заполнения.");
                return;
            }

            double price = Double.parseDouble(priceField.getText().replace(',', '.').trim());
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

            AdCreationRequest request = new AdCreationRequest(titleField.getText().trim(), fullDescription, price, categoryComboBox.getSelectionModel().getSelectedItem().getId(), currentUser.getUserId(), finalImagePaths);

            if (adToEdit == null) {
                commandManager.createAd(request);
            } else {
                commandManager.updateAd(adToEdit.getAdId(), request, currentUser.getUserId());
            }

            showSuccessAndReturn("Объявление успешно сохранено!");

        } catch (Exception e) {
            showError("Произошла ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseAndApplyDecoratorMetadata(String metadata) {
        if (metadata == null || metadata.isEmpty()) return;
        Stream.of(metadata.split(";")).forEach(pair -> {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length < 2) return;
            String key = keyValue[0];
            String value = keyValue[1];
            switch (key) {
                case "premium": premiumCheckBox.setSelected(Boolean.parseBoolean(value)); break;
                case "urgent": urgentCheckBox.setSelected(Boolean.parseBoolean(value)); break;
                case "discount": discountCheckBox.setSelected(Boolean.parseBoolean(value)); handleDiscountToggle(); break;
                case "discountPercentage": discountPercentageField.setText(value); break;
                case "discountReason": discountReasonField.setText(value); break;
                case "warranty": warrantyCheckBox.setSelected(Boolean.parseBoolean(value)); handleWarrantyToggle(); break;
                case "warrantyMonths": warrantyMonthsField.setText(value); break;
                case "warrantyType": warrantyTypeField.setText(value); break;
                case "delivery": deliveryCheckBox.setSelected(Boolean.parseBoolean(value)); handleDeliveryToggle(); break;
                case "freeDelivery": freeDeliveryCheckBox.setSelected(Boolean.parseBoolean(value)); handleFreeDeliveryToggle(); break;
                case "deliveryCost": deliveryCostField.setText(value); break;
                case "deliveryInfo": deliveryInfoField.setText(value); break;
            }
        });
    }

    private String buildDecoratorMetadata() {
        StringBuilder sb = new StringBuilder();
        sb.append("premium:").append(premiumCheckBox.isSelected()).append(";");
        sb.append("urgent:").append(urgentCheckBox.isSelected()).append(";");
        sb.append("discount:").append(discountCheckBox.isSelected()).append(";");
        if (discountCheckBox.isSelected()) {
            sb.append("discountPercentage:").append(discountPercentageField.getText()).append(";");
            sb.append("discountReason:").append(discountReasonField.getText()).append(";");
        }
        sb.append("warranty:").append(warrantyCheckBox.isSelected()).append(";");
        if(warrantyCheckBox.isSelected()){
            sb.append("warrantyMonths:").append(warrantyMonthsField.getText()).append(";");
            sb.append("warrantyType:").append(warrantyTypeField.getText()).append(";");
        }
        sb.append("delivery:").append(deliveryCheckBox.isSelected()).append(";");
        if(deliveryCheckBox.isSelected()){
            sb.append("freeDelivery:").append(freeDeliveryCheckBox.isSelected()).append(";");
            sb.append("deliveryCost:").append(deliveryCostField.getText()).append(";");
            sb.append("deliveryInfo:").append(deliveryInfoField.getText()).append(";");
        }
        return sb.toString();
    }


    @FXML private void handleUpdatePreview() {
        if (previewArea == null) return;
        try {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            String priceStr = priceField.getText().replace(',', '.').trim();
            if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
                previewArea.setText("Заполните все основные поля для предпросмотра");
                return;
            }
            Ad tempAd = new Ad();
            tempAd.setTitle(title);
            tempAd.setDescription(description);
            tempAd.setPrice(Double.parseDouble(priceStr));
            tempAd.setStatus("ACTIVE");
            AdComponent decoratedAd = createDecoratedAd(tempAd);
            String preview = decoratedAd.getDisplayInfo() + "\n\n" + "=".repeat(50) + "\nЗаголовок: " + decoratedAd.getFormattedTitle() + String.format("\nИтоговая цена: %.2f грн", decoratedAd.getCalculatedPrice());
            previewArea.setText(preview);
        } catch (Exception e) {
            previewArea.setText("Ошибка создания предпросмотра: " + e.getMessage());
        }
    }

    private void updatePreviewAutomatically() { handleUpdatePreview(); }
    @FXML private void handleDiscountToggle() {
        boolean selected = discountCheckBox.isSelected();
        discountOptionsBox.setVisible(selected);
        discountOptionsBox.setManaged(selected);
        if (!selected) { discountPercentageField.clear(); discountReasonField.clear(); }
        updatePreviewAutomatically();
    }
    @FXML private void handleWarrantyToggle() {
        boolean selected = warrantyCheckBox.isSelected();
        warrantyOptionsBox.setVisible(selected);
        warrantyOptionsBox.setManaged(selected);
        if (!selected) { warrantyMonthsField.clear(); warrantyTypeField.clear(); }
        updatePreviewAutomatically();
    }
    @FXML private void handleDeliveryToggle() {
        boolean selected = deliveryCheckBox.isSelected();
        deliveryOptionsBox.setVisible(selected);
        deliveryOptionsBox.setManaged(selected);
        if (!selected) { freeDeliveryCheckBox.setSelected(false); deliveryCostField.clear(); deliveryInfoField.clear(); }
        updatePreviewAutomatically();
    }
    @FXML private void handleFreeDeliveryToggle() {
        boolean selected = freeDeliveryCheckBox.isSelected();
        deliveryCostBox.setVisible(!selected);
        deliveryCostBox.setManaged(!selected);
        if (selected) { deliveryCostField.clear(); }
        updatePreviewAutomatically();
    }

    private void setupDecoratorValidation() {
        discountOptionsBox.setVisible(false); discountOptionsBox.setManaged(false);
        warrantyOptionsBox.setVisible(false); warrantyOptionsBox.setManaged(false);
        deliveryOptionsBox.setVisible(false); deliveryOptionsBox.setManaged(false);
        discountCheckBox.setOnAction(event -> handleDiscountToggle());
        warrantyCheckBox.setOnAction(event -> handleWarrantyToggle());
        deliveryCheckBox.setOnAction(event -> handleDeliveryToggle());
        freeDeliveryCheckBox.setOnAction(event -> handleFreeDeliveryToggle());
    }

    private void showError(String message) {
        if (errorLabel != null) { errorLabel.setText(message); errorLabel.setVisible(true); }
        else { new Alert(Alert.AlertType.ERROR, message).showAndWait(); }
    }

    private AdComponent createDecoratedAd(Ad ad) {
        Double discountPercentage = null; String discountReason = null;
        if (discountCheckBox.isSelected()) { try { discountPercentage = Double.parseDouble(discountPercentageField.getText().trim()); discountReason = discountReasonField.getText().trim(); } catch (Exception e) { /* ignore */ } }
        Integer warrantyMonths = null; String warrantyType = null;
        if (warrantyCheckBox.isSelected()) { try { warrantyMonths = Integer.parseInt(warrantyMonthsField.getText().trim()); warrantyType = warrantyTypeField.getText().trim(); } catch (Exception e) { /* ignore */ } }
        Boolean freeDelivery = null; Double deliveryCost = null; String deliveryInfo = null;
        if (deliveryCheckBox.isSelected()) { freeDelivery = freeDeliveryCheckBox.isSelected(); deliveryInfo = deliveryInfoField.getText().trim(); if (!freeDelivery) { try { deliveryCost = Double.parseDouble(deliveryCostField.getText().trim()); } catch (Exception e) { /* ignore */ } } }
        return AdDecoratorFactory.createFullyDecoratedAd(ad, premiumCheckBox.isSelected(), urgentCheckBox.isSelected(), discountPercentage, discountReason, warrantyMonths, warrantyType, freeDelivery, deliveryCost, deliveryInfo);
    }

    private void setupPreviewListeners() {
        titleField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        descriptionArea.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        priceField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        categoryComboBox.valueProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        premiumCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        urgentCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        discountCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        discountPercentageField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        discountReasonField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        warrantyCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        warrantyMonthsField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        warrantyTypeField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        deliveryCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        freeDeliveryCheckBox.selectedProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        deliveryCostField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
        deliveryInfoField.textProperty().addListener((obs, o, n) -> updatePreviewAutomatically());
    }

    private void setupImageStorageDir() {
        try { Files.createDirectories(Paths.get(IMAGE_STORAGE_DIR)); }
        catch (IOException e) { System.err.println("Could not create image storage directory: " + e.getMessage()); }
    }

    private void setupCategoryComboBox() {
        categoryComboBox.setItems(categoryItems);
        categoryComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(CategoryDisplayItem item) { return item == null ? null : item.getDisplayName(); }
            @Override public CategoryDisplayItem fromString(String string) { return categoryItems.stream().filter(i -> i.getDisplayName().equals(string)).findFirst().orElse(null); }
        });
    }

    private void setupPriceFieldValidation() {
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([.,]\\d{0,2})?")) { priceField.setText(oldValue); }
        });
    }

    private void loadCategories() {
        if (MainGuiApp.categoryService == null) { showError("Ошибка загрузки категорий."); return; }
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

    @FXML private void handleAddPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбрать фото");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg"));
        List<File> files = fileChooser.showOpenMultipleDialog(addPhotoButton.getScene().getWindow());
        if (files != null) {
            int limit = 5 - selectedImageFiles.size() - existingImagePaths.size();
            if (files.size() > limit) { showError("Можно добавить еще максимум " + limit + " фото."); }
            for (int i = 0; i < files.size() && i < limit; i++) {
                File file = files.get(i);
                if (!selectedImageFiles.contains(file)) {
                    selectedImageFiles.add(file);
                    addImageToPreview(file);
                }
            }
            if (selectedImageFiles.size() + existingImagePaths.size() >= 5) { addPhotoButton.setDisable(true); }
        }
    }

    private void addImageToPreview(File file) {
        try {
            ImageView imageView = new ImageView(new Image(file.toURI().toString(), 100, 100, true, true));
            Button removeButton = new Button("X");
            VBox imageContainer = new VBox(5, imageView, removeButton);
            removeButton.setOnAction(event -> {
                photoPreviewBox.getChildren().remove(imageContainer);
                selectedImageFiles.remove(file);
                addPhotoButton.setDisable(false);
            });
            photoPreviewBox.getChildren().add(imageContainer);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addExistingImageToPreview(String imagePath) {
        try {
            File file = new File(imagePath);
            ImageView imageView = new ImageView(new Image(file.toURI().toString(), 100, 100, true, true));
            Button removeButton = new Button("X");
            VBox imageContainer = new VBox(5, imageView, removeButton);
            removeButton.setOnAction(event -> {
                photoPreviewBox.getChildren().remove(imageContainer);
                existingImagePaths.remove(imagePath);
                addPhotoButton.setDisable(false);
            });
            photoPreviewBox.getChildren().add(imageContainer);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static class CategoryDisplayItem {
        private final String id;
        private final String displayName;
        public CategoryDisplayItem(String id, String displayName) { this.id = id; this.displayName = displayName; }
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
    }
}
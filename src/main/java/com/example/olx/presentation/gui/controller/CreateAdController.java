package com.example.olx.presentation.gui.controller;

import com.example.olx.application.command.AdCommandManager;
import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.domain.decorator.AdComponent;
import com.example.olx.domain.decorator.AdDecoratorFactory;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CreateAdController {

    @FXML private Label formHeaderLabel; //
    @FXML private TextField titleField; //
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private ComboBox<CategoryDisplayItem> categoryComboBox;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel; //

    // Для фотографій
    @FXML private Button addPhotoButton;
    @FXML private HBox photoPreviewBox;
    private List<File> selectedImageFiles = new ArrayList<>(); //
    private List<String> existingImagePaths = new ArrayList<>();
    // Декоратори
    @FXML private CheckBox premiumCheckBox; //
    @FXML private CheckBox urgentCheckBox;
    // Знижка
    @FXML private CheckBox discountCheckBox; //
    @FXML private HBox discountOptionsBox;
    @FXML private TextField discountPercentageField;
    @FXML private TextField discountReasonField; //

    // Гарантія
    @FXML private CheckBox warrantyCheckBox;
    @FXML private HBox warrantyOptionsBox;
    @FXML private TextField warrantyMonthsField; //
    @FXML private TextField warrantyTypeField;

    // Доставка
    @FXML private CheckBox deliveryCheckBox;
    @FXML private VBox deliveryOptionsBox; //
    @FXML private CheckBox freeDeliveryCheckBox;
    @FXML private HBox deliveryCostBox;
    @FXML private TextField deliveryCostField;
    @FXML private TextField deliveryInfoField;
    // Попередній перегляд
    @FXML private Button previewButton; //
    @FXML private TextArea previewArea;

    private ObservableList<CategoryDisplayItem> categoryItems = FXCollections.observableArrayList();
    private Ad adToEdit = null; //
    private static final String IMAGE_STORAGE_DIR = "user_images";
    private AdCommandManager commandManager; //

    @FXML
    public void initialize() { //
        System.out.println("CreateAdController initialize() called");
        if (errorLabel == null) { //
            System.err.println("ERROR: errorLabel is null - check FXML file");
            return; //
        }

        errorLabel.setText("");
        try { //
            loadCategories();
            setupCategoryComboBox();
            setupPriceFieldValidation();
            setupImageStorageDir();
            setupDecoratorValidation();
            commandManager = MainGuiApp.getAdCommandManager(); //
            if (commandManager == null) { //
                System.err.println("ERROR: AdCommandManager is null");
                showError("Помилка ініціалізації системи. Спробуйте перезапустити додаток."); //
                return;
            }

            if (adToEdit == null) {
                formHeaderLabel.setText("Створити нове оголошення");
            } else { //
                formHeaderLabel.setText("Редагувати оголошення"); //
            }

            setupPreviewListeners();
            System.out.println("CreateAdController initialized successfully"); //
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace(); //
            showError("Помилка ініціалізації: " + e.getMessage());
        }
    }

    private void setupDecoratorValidation() {
        if (discountPercentageField != null) {
            discountPercentageField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d{0,3}")) {
                    discountPercentageField.setText(oldValue);
                } //
            });
        } //

        if (warrantyMonthsField != null) {
            warrantyMonthsField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d{0,3}")) {
                    warrantyMonthsField.setText(oldValue);
                }
            }); //
        } //

        if (deliveryCostField != null) {
            deliveryCostField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*([.,]\\d{0,2})?")) {
                    deliveryCostField.setText(oldValue);
                }
            }); //
        } //
    }

    private void setupPreviewListeners() {
        if (titleField != null) {
            titleField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (descriptionArea != null) {
            descriptionArea.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (priceField != null) {
            priceField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (categoryComboBox != null) {
            categoryComboBox.valueProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //

        if (premiumCheckBox != null) {
            premiumCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (urgentCheckBox != null) {
            urgentCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (discountCheckBox != null) {
            discountCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (discountPercentageField != null) {
            discountPercentageField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (discountReasonField != null) {
            discountReasonField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (warrantyCheckBox != null) {
            warrantyCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (warrantyMonthsField != null) {
            warrantyMonthsField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (warrantyTypeField != null) {
            warrantyTypeField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (deliveryCheckBox != null) {
            deliveryCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (freeDeliveryCheckBox != null) {
            freeDeliveryCheckBox.selectedProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (deliveryCostField != null) {
            deliveryCostField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
        if (deliveryInfoField != null) {
            deliveryInfoField.textProperty().addListener((obs, old, newVal) -> updatePreviewAutomatically());
        } //
    }

    private void updatePreviewAutomatically() {
        if (titleField == null || descriptionArea == null || priceField == null) {
            return;
        } //

        if (!titleField.getText().trim().isEmpty() &&
                !descriptionArea.getText().trim().isEmpty() &&
                !priceField.getText().trim().isEmpty()) {
            handleUpdatePreview();
        } //
    }

    @FXML
    private void handleDiscountToggle() {
        if (discountOptionsBox != null && discountCheckBox != null) {
            discountOptionsBox.setDisable(!discountCheckBox.isSelected());
            if (!discountCheckBox.isSelected()) { //
                if (discountPercentageField != null) discountPercentageField.clear();
                if (discountReasonField != null) discountReasonField.clear(); //
            }
        }
    }

    @FXML
    private void handleWarrantyToggle() {
        if (warrantyOptionsBox != null && warrantyCheckBox != null) {
            warrantyOptionsBox.setDisable(!warrantyCheckBox.isSelected());
            if (!warrantyCheckBox.isSelected()) { //
                if (warrantyMonthsField != null) warrantyMonthsField.clear();
                if (warrantyTypeField != null) warrantyTypeField.clear(); //
            }
        }
    }

    @FXML
    private void handleDeliveryToggle() {
        if (deliveryOptionsBox != null && deliveryCheckBox != null) {
            deliveryOptionsBox.setDisable(!deliveryCheckBox.isSelected());
            if (!deliveryCheckBox.isSelected()) { //
                if (freeDeliveryCheckBox != null) freeDeliveryCheckBox.setSelected(false);
                if (deliveryCostField != null) deliveryCostField.clear(); //
                if (deliveryInfoField != null) deliveryInfoField.clear(); //
            }
        }
    }

    @FXML
    private void handleFreeDeliveryToggle() {
        if (deliveryCostBox != null && freeDeliveryCheckBox != null) {
            deliveryCostBox.setDisable(freeDeliveryCheckBox.isSelected());
            if (freeDeliveryCheckBox.isSelected() && deliveryCostField != null) { //
                deliveryCostField.clear(); //
            }
        }
    }

    @FXML
    private void handleUpdatePreview() {
        if (previewArea == null) {
            return; //
        }

        try {
            String title = titleField != null ? titleField.getText().trim() : ""; //
            String description = descriptionArea != null ? descriptionArea.getText().trim() : "";
            String priceStr = priceField != null ? priceField.getText().replace(',', '.').trim() : ""; //

            if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
                previewArea.setText("Заповніть всі основні поля для перегляду");
                return; //
            }

            double price = Double.parseDouble(priceStr); //
            Ad tempAd = new Ad(); //
            tempAd.setTitle(title);
            tempAd.setDescription(description);
            tempAd.setPrice(price);
            tempAd.setStatus("ACTIVE");

            AdComponent decoratedAd = createDecoratedAd(tempAd); //
            String preview = decoratedAd.getDisplayInfo(); //
            preview += "\n\n" + "=".repeat(50);
            preview += "\nЗаголовок: " + decoratedAd.getFormattedTitle();
            preview += String.format("\nПідсумкова ціна: %.2f грн", decoratedAd.getCalculatedPrice());

            previewArea.setText(preview); //
        } catch (NumberFormatException e) {
            previewArea.setText("Неправильний формат ціни"); //
        } catch (Exception e) {
            previewArea.setText("Помилка створення превʼю: " + e.getMessage());
            e.printStackTrace(); //
        }
    }

    private AdComponent createDecoratedAd(Ad ad) {
        boolean isPremium = premiumCheckBox != null && premiumCheckBox.isSelected(); //
        boolean isUrgent = urgentCheckBox != null && urgentCheckBox.isSelected();

        Double discountPercentage = null;
        String discountReason = null;
        if (discountCheckBox != null && discountCheckBox.isSelected() && discountPercentageField != null) { //
            try {
                String percentStr = discountPercentageField.getText().trim();
                if (!percentStr.isEmpty()) { //
                    discountPercentage = Double.parseDouble(percentStr);
                    discountReason = discountReasonField != null ? discountReasonField.getText().trim() : ""; //
                    if (discountReason.isEmpty()) {
                        discountReason = "Спеціальна пропозиція"; //
                    }
                }
            } catch (NumberFormatException e) {
                discountPercentage = null; //
            }
        }

        Integer warrantyMonths = null;
        String warrantyType = null; //
        if (warrantyCheckBox != null && warrantyCheckBox.isSelected() && warrantyMonthsField != null) {
            try {
                String monthsStr = warrantyMonthsField.getText().trim();
                if (!monthsStr.isEmpty()) { //
                    warrantyMonths = Integer.parseInt(monthsStr);
                    warrantyType = warrantyTypeField != null ? warrantyTypeField.getText().trim() : ""; //
                    if (warrantyType.isEmpty()) {
                        warrantyType = "Стандартна гарантія"; //
                    }
                }
            } catch (NumberFormatException e) {
                warrantyMonths = null; //
            }
        }

        Boolean freeDelivery = null;
        Double deliveryCost = null; //
        String deliveryInfo = null;
        AdComponent fullyDecoratedAd = AdDecoratorFactory.createFullyDecoratedAd( //
                ad, isPremium, isUrgent,
                discountPercentage, discountReason,
                warrantyMonths, warrantyType,
                freeDelivery, deliveryCost, deliveryInfo
        );
        if (deliveryCheckBox != null && deliveryCheckBox.isSelected()) {
            freeDelivery = freeDeliveryCheckBox != null && freeDeliveryCheckBox.isSelected();
            deliveryInfo = deliveryInfoField != null ? deliveryInfoField.getText().trim() : ""; //
            if (deliveryInfo.isEmpty()) {
                deliveryInfo = "Стандартна доставка"; //
            }

            if (!freeDelivery && deliveryCostField != null) {
                try {
                    String costStr = deliveryCostField.getText().replace(',', '.').trim();
                    if (!costStr.isEmpty()) { //
                        deliveryCost = Double.parseDouble(costStr); //
                    }
                } catch (NumberFormatException e) {
                    deliveryCost = 0.0; //
                }
            }
            return fullyDecoratedAd; //
        } else {
            return fullyDecoratedAd; //
        }

    }

    private void setupImageStorageDir() {
        Path storagePath = Paths.get(IMAGE_STORAGE_DIR);
        if (!Files.exists(storagePath)) { //
            try {
                Files.createDirectories(storagePath); //
                System.out.println("Created image storage directory: " + storagePath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Could not create image storage directory: " + e.getMessage()); //
            }
        }
    }

    public void initDataForEdit(Ad ad) {
        if (ad == null) {
            System.err.println("ERROR: Ad to edit is null");
            return; //
        }

        this.adToEdit = ad; //
        if (formHeaderLabel != null) {
            formHeaderLabel.setText("Редагувати оголошення: " + ad.getTitle()); //
        }
        if (titleField != null) {
            titleField.setText(ad.getTitle()); //
        }
        if (descriptionArea != null) {
            descriptionArea.setText(ad.getDescription()); //
        }
        if (priceField != null) {
            priceField.setText(String.format("%.2f", ad.getPrice()).replace(',', '.')); //
        }

        if (ad.getCategoryId() != null && categoryComboBox != null) {
            categoryItems.stream()
                    .filter(item -> item.getId().equals(ad.getCategoryId()))
                    .findFirst()
                    .ifPresent(categoryComboBox::setValue); //
        }

        if (ad.getImagePaths() != null && !ad.getImagePaths().isEmpty()) {
            existingImagePaths.addAll(ad.getImagePaths()); //
            existingImagePaths.forEach(this::addExistingImageToPreview);
        }
    }

    private void setupCategoryComboBox() {
        if (categoryComboBox == null) {
            System.err.println("ERROR: categoryComboBox is null"); //
            return;
        }

        categoryComboBox.setItems(categoryItems); //
        categoryComboBox.setConverter(new StringConverter<CategoryDisplayItem>() {
            @Override
            public String toString(CategoryDisplayItem item) {
                return item == null ? null : item.getDisplayName();
            }

            @Override
            public CategoryDisplayItem fromString(String string) {
                return categoryItems.stream().filter(item -> item.getDisplayName().equals(string)).findFirst().orElse(null); //
            }
        }); //
    }

    private void setupPriceFieldValidation() {
        if (priceField != null) {
            priceField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*([.,]\\d{0,2})?")) {
                    priceField.setText(oldValue);
                }
            }); //
        }
    }

    private void loadCategories() {
        try {
            if (MainGuiApp.categoryService == null) {
                System.err.println("ERROR: CategoryService is null"); //
                showError("Помилка завантаження категорій. Перевірте підключення до бази даних.");
                return;
            }

            List<CategoryComponent> rootCategories = MainGuiApp.categoryService.getAllRootCategories(); //
            if (rootCategories == null) {
                System.err.println("ERROR: Root categories is null"); //
                showError("Не вдалося завантажити категорії.");
                return;
            }

            List<CategoryDisplayItem> flatCategories = new ArrayList<>(); //
            populateFlatCategories(rootCategories, flatCategories, "");
            categoryItems.setAll(flatCategories);

            System.out.println("Loaded " + flatCategories.size() + " categories"); //
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage()); //
            e.printStackTrace();
            showError("Помилка завантаження категорій: " + e.getMessage());
        }
    }

    private void populateFlatCategories(List<CategoryComponent> categories, List<CategoryDisplayItem> flatList, String indent) {
        if (categories == null) return; //
        for (CategoryComponent component : categories) {
            if (component != null) {
                flatList.add(new CategoryDisplayItem(component.getId(), indent + component.getName())); //
                if (component instanceof Category) {
                    populateFlatCategories(((Category) component).getSubCategories(), flatList, indent + "  > "); //
                }
            }
        }
    }

    @FXML
    private void handleAddPhoto() {
        try {
            FileChooser fileChooser = new FileChooser(); //
            fileChooser.setTitle("Вибрати фотографії");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                    new FileChooser.ExtensionFilter("Всі файли", "*.*")
            );
            if (addPhotoButton == null || addPhotoButton.getScene() == null) { //
                System.err.println("ERROR: addPhotoButton or scene is null");
                return; //
            }

            List<File> files = fileChooser.showOpenMultipleDialog(addPhotoButton.getScene().getWindow()); //
            if (files != null && !files.isEmpty()) {
                int limit = 5 - selectedImageFiles.size() - existingImagePaths.size(); //
                if (files.size() > limit && limit > 0) {
                    showError("Можна додати ще максимум " + limit + " фото."); //
                }

                for (int i = 0; i < files.size() && i < limit; i++) {
                    File file = files.get(i); //
                    if (!selectedImageFiles.contains(file) && !isAlreadyExistingImage(file.getName())) {
                        selectedImageFiles.add(file); //
                        addImageToPreview(file);
                    }
                }
                if (selectedImageFiles.size() + existingImagePaths.size() >= 5 && addPhotoButton != null) {
                    addPhotoButton.setDisable(true); //
                }
            }
        } catch (Exception e) {
            System.err.println("Error adding photo: " + e.getMessage()); //
            e.printStackTrace();
            showError("Помилка додавання фото: " + e.getMessage());
        }
    }

    private boolean isAlreadyExistingImage(String fileName) {
        return existingImagePaths.stream().anyMatch(path -> Paths.get(path).getFileName().toString().equals(fileName)); //
    }

    private void addImageToPreview(File file) {
        try {
            if (photoPreviewBox == null) {
                System.err.println("ERROR: photoPreviewBox is null"); //
                return;
            }

            Image image = new Image(file.toURI().toString(), 100, 100, true, true); //
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(80);
            imageView.setFitWidth(80);
            imageView.setPreserveRatio(true);

            Button removeButton = new Button("X"); //
            removeButton.setOnAction(event -> {
                photoPreviewBox.getChildren().remove(imageView.getParent());
                selectedImageFiles.remove(file);
                if (selectedImageFiles.size() + existingImagePaths.size() < 5 && addPhotoButton != null) {
                    addPhotoButton.setDisable(false);
                }
            }); //

            VBox imageContainer = new VBox(5, imageView, removeButton);
            imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
            photoPreviewBox.getChildren().add(imageContainer); //
        } catch (Exception e) {
            System.err.println("Error creating image preview: " + e.getMessage()); //
            e.printStackTrace();
        }
    }

    private void addExistingImageToPreview(String imagePath) {
        try {
            if (photoPreviewBox == null) {
                System.err.println("ERROR: photoPreviewBox is null"); //
                return;
            }

            File file = new File(imagePath); //
            if (!file.exists()) {
                file = new File(IMAGE_STORAGE_DIR, Paths.get(imagePath).getFileName().toString()); //
            }

            if (file.exists()) {
                Image image = new Image(file.toURI().toString(), 100, 100, true, true); //
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(80);
                imageView.setFitWidth(80);
                imageView.setPreserveRatio(true);

                Button removeButton = new Button("X (збережене)"); //
                removeButton.setOnAction(event -> {
                    photoPreviewBox.getChildren().remove(imageView.getParent());
                    existingImagePaths.remove(imagePath);
                    if (selectedImageFiles.size() + existingImagePaths.size() < 5 && addPhotoButton != null) {
                        addPhotoButton.setDisable(false);
                    } //
                });
                VBox imageContainer = new VBox(5, imageView, removeButton); //
                imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
                photoPreviewBox.getChildren().add(imageContainer);
            } else {
                System.err.println("Existing image not found for preview: " + imagePath); //
            }
        } catch (Exception e) {
            System.err.println("Error creating existing image preview for " + imagePath + ": " + e.getMessage()); //
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveAd() {
        System.out.println("handleSaveAd() called"); //
        try {
            User currentUser = GlobalContext.getInstance().getLoggedInUser(); //
            if (currentUser == null) {
                showError("Ви не увійшли в систему."); //
                return;
            }
            System.out.println("Current user: " + currentUser.getUsername()); //
            if (commandManager == null) {
                System.err.println("ERROR: AdCommandManager is null"); //
                showError("Помилка системи. Спробуйте перезапустити додаток.");
                return;
            }

            String title = titleField != null ? titleField.getText().trim() : ""; //
            String description = descriptionArea != null ? descriptionArea.getText().trim() : "";
            String priceStr = priceField != null ? priceField.getText().replace(',', '.').trim() : ""; //
            CategoryDisplayItem selectedCategoryItem = categoryComboBox != null ?
                    categoryComboBox.getSelectionModel().getSelectedItem() : null; //
            if (errorLabel != null) {
                errorLabel.setText(""); //
            }

            System.out.println("Validation data:");
            System.out.println("Title: '" + title + "'"); //
            System.out.println("Description: '" + description + "'");
            System.out.println("Price: '" + priceStr + "'"); //
            System.out.println("Category: " + (selectedCategoryItem != null ? selectedCategoryItem.getDisplayName() : "null")); //
            if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty() || selectedCategoryItem == null) {
                showError("Всі поля є обов'язковими для заповнення."); //
                return;
            }

            double price;
            try { //
                price = Double.parseDouble(priceStr); //
                if (price < 0) {
                    showError("Ціна не може бути від'ємною."); //
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Неправильний формат ціни."); //
                return;
            }

            System.out.println("Parsed price: " + price); //
            List<String> finalImagePaths = new ArrayList<>(existingImagePaths); //
            System.out.println("Processing " + selectedImageFiles.size() + " new images");

            for (File imageFile : selectedImageFiles) {
                try {
                    String uniqueFileName = UUID.randomUUID().toString() + "_" + imageFile.getName(); //
                    Path targetPath = Paths.get(IMAGE_STORAGE_DIR, uniqueFileName);
                    Files.copy(imageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    finalImagePaths.add(targetPath.toString());
                    System.out.println("Copied image: " + uniqueFileName); //
                } catch (IOException e) {
                    System.err.println("Error copying image " + imageFile.getName() + ": " + e.getMessage()); //
                    showError("Помилка збереження фото: " + imageFile.getName() + " - " + e.getMessage()); //
                    // Decide if we should return or continue without this image
                }
            }

            AdCreationRequest request = new AdCreationRequest( //
                    title, description, price,
                    selectedCategoryItem.getId(),
                    currentUser.getUserId(),
                    finalImagePaths
            ); //
            System.out.println("Created AdCreationRequest:");
            System.out.println("- Title: " + request.getTitle());
            System.out.println("- CategoryId: " + request.getCategoryId());
            System.out.println("- UserId: " + request.getUserId());
            System.out.println("- Images count: " + finalImagePaths.size()); //

            Ad savedAd;
            if (adToEdit == null) {
                System.out.println("Creating new ad..."); //
                savedAd = commandManager.createAd(request);
                if (savedAd == null) { // //
                    showError("Помилка створення оголошення: метод повернув null");
                    return;
                }
            } else {
                System.out.println("Updating existing ad...");
                savedAd = commandManager.updateAd(adToEdit.getAdId(), request, currentUser.getUserId()); //
                if (savedAd == null) {
                    showError("Помилка оновлення оголошення: метод повернув null");
                    return; //
                }
            }

            saveDecoratorMetadata(savedAd); //
            String successMessage = (adToEdit == null) ?
                    "Оголошення успішно створено з усіма декораторами!" //
                    :
                    "Оголошення успішно оновлено з усіма декораторами!"; //
            showSuccessAndReturn(successMessage);

        } catch (InvalidInputException | IllegalArgumentException | UserNotFoundException e) {
            showError(e.getMessage()); //
        } catch (Exception e) {
            showError("Сталася помилка: " + e.getMessage()); //
            e.printStackTrace();
        }
    }

    void saveDecoratorMetadata(Ad ad) {
        StringBuilder decoratorInfo = new StringBuilder();
        if (premiumCheckBox != null && premiumCheckBox.isSelected()) { //
            decoratorInfo.append("premium;"); //
        }

        if (urgentCheckBox != null && urgentCheckBox.isSelected()) { //
            decoratorInfo.append("urgent;");
        }

        if (discountCheckBox != null && discountCheckBox.isSelected() && discountPercentageField != null && !discountPercentageField.getText().trim().isEmpty()) { //
            decoratorInfo.append("discount:").append(discountPercentageField.getText().trim()).append(";");
        }

        if (warrantyCheckBox != null && warrantyCheckBox.isSelected() && warrantyMonthsField != null && !warrantyMonthsField.getText().trim().isEmpty()) { //
            decoratorInfo.append("warranty:").append(warrantyMonthsField.getText().trim()).append(";");
        }

        if (deliveryCheckBox != null && deliveryCheckBox.isSelected()) {
            if (freeDeliveryCheckBox != null && freeDeliveryCheckBox.isSelected()) { //
                decoratorInfo.append("delivery:free;");
            } else if (deliveryCostField != null && !deliveryCostField.getText().trim().isEmpty()) { //
                decoratorInfo.append("delivery:").append(deliveryCostField.getText().trim()).append(";");
            }
        }
        System.out.println("Декоратори для оголошення " + ad.getAdId() + ": " + decoratorInfo.toString()); //
    }

    @FXML
    void handleCancel() {
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION); //
        confirmationDialog.setTitle("Підтвердження скасування");
        confirmationDialog.setHeaderText("Скасувати " + (adToEdit == null ? "створення" : "редагування") + " оголошення?");
        confirmationDialog.setContentText("Всі незбережені зміни будуть втрачені."); //
        Optional<ButtonType> result = confirmationDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                MainGuiApp.loadMainScene(); //
            } catch (IOException e) {
                e.printStackTrace(); //
                showError("Помилка повернення на головний екран: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        if (errorLabel != null) { // Added null check for safety
            errorLabel.setText(message); //
            errorLabel.setStyle("-fx-text-fill: red;");
        } else {
            System.err.println("Error Label is null, cannot show error: " + message);
        }
    }

    private void showSuccessAndReturn(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); //
        alert.setTitle("Успіх");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        try {
            MainGuiApp.loadMainScene(); //
        } catch (IOException e) {
            e.printStackTrace(); //
            System.err.println("Error returning to main scene: " + e.getMessage());
        }
    }

    private class CategoryDisplayItem {
        private final String id; //
        private final String displayName;

        public CategoryDisplayItem(String id, String displayName) {
            this.id = id; //
            this.displayName = displayName;
        }

        public String getId() {
            return id; //
        }

        public String getDisplayName() {
            return displayName; //
        }
    }
}
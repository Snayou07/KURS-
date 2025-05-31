// src/main/java/com/example/olx/presentation/gui/controller/CreateAdController.java
package com.example.olx.presentation.gui.controller;

import com.example.olx.application.command.AdCommandManager;
import com.example.olx.application.dto.AdCreationRequest;
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
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

        // Отримуємо Command Manager з головного класу
        commandManager = MainGuiApp.getAdCommandManager();

        if (adToEdit == null) {
            formHeaderLabel.setText("Створити нове оголошення");
        } else {
            formHeaderLabel.setText("Редагувати оголошення");
        }
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

        AdCreationRequest request = new AdCreationRequest(
                title, description, price,
                selectedCategoryItem.getId(),
                currentUser.getUserId(),
                finalImagePaths
        );

        try {
            if (adToEdit == null) {
                // Використовуємо Command Manager для створення оголошення
                commandManager.createAd(request);
                showSuccessAndReturn("Оголошення успішно створено!");
            } else {
                // Використовуємо Command Manager для оновлення оголошення
                commandManager.updateAd(adToEdit.getAdId(), request, currentUser.getUserId());
                showSuccessAndReturn("Оголошення успішно оновлено!");
            }
        } catch (InvalidInputException | IllegalArgumentException | UserNotFoundException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Сталася помилка: " + e.getMessage());
            e.printStackTrace();
        }
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
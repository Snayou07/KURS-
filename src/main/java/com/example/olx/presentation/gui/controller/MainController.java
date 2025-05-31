package com.example.olx.presentation.gui.controller;

import com.example.olx.application.command.AdCommandManager;
import com.example.olx.application.command.CommandFactory;
import com.example.olx.application.command.CommandInvoker;
import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.Category;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.model.User;
import com.example.olx.presentation.gui.MainGuiApp;
import com.example.olx.presentation.gui.util.GlobalContext;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private BorderPane mainBorderPane;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button createAdButton;
    @FXML private Label loggedInUserLabel;
    @FXML private Button logoutButton;
    @FXML private TreeView<CategoryComponent> categoryTreeView;
    @FXML private Label currentCategoryLabel;
    @FXML private ListView<Ad> adListView;
    @FXML private HBox paginationControls;

    // Command pattern components
    @FXML private Button undoButton;
    @FXML private Button redoButton;
    @FXML private Button clearHistoryButton;
    @FXML private ListView<String> commandHistoryListView;

    private AdCommandManager commandManager;
    private ObservableList<Ad> adsObservableList = FXCollections.observableArrayList();
    private ObservableList<String> commandHistoryObservableList = FXCollections.observableArrayList();
    private String currentSelectedCategoryId = null;

    @FXML
    public void initialize() {
        User currentUser = GlobalContext.getInstance().getLoggedInUser();
        if (currentUser != null) {
            loggedInUserLabel.setText("Користувач: " + currentUser.getUsername());
            createAdButton.setDisable(false);
            logoutButton.setDisable(false);
        } else {
            try {
                MainGuiApp.loadLoginScene();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Platform.exit();
            }
        }

        initializeCommandManager();
        setupCategoryTree();
        setupAdListView();
        setupCommandHistoryView();
        loadAds(null);
        updateCommandButtons();

        // Обробник вибору категорії в дереві
        categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        CategoryComponent selectedCategory = newValue.getValue();
                        currentSelectedCategoryId = selectedCategory.getId();
                        currentCategoryLabel.setText("Оголошення в категорії: " + selectedCategory.getName());
                        loadAds(selectedCategory.getId());
                    } else {
                        currentSelectedCategoryId = null;
                        currentCategoryLabel.setText("Всі оголошення");
                        loadAds(null);
                    }
                });
    }

    private void initializeCommandManager() {
        CommandInvoker commandInvoker = new CommandInvoker();
        CommandFactory commandFactory = new CommandFactory(MainGuiApp.adService);
        commandManager = new AdCommandManager(commandInvoker, commandFactory);
    }

    private void setupCommandHistoryView() {
        if (commandHistoryListView != null) {
            commandHistoryListView.setItems(commandHistoryObservableList);
            commandHistoryListView.setPrefHeight(150);
        }
    }

    private void updateCommandButtons() {
        if (undoButton != null) {
            undoButton.setDisable(!commandManager.canUndo());
        }
        if (redoButton != null) {
            redoButton.setDisable(!commandManager.canRedo());
        }

        // Оновлюємо історію команд
        commandHistoryObservableList.setAll(commandManager.getCommandHistory());
    }

    private void setupCategoryTree() {
        List<CategoryComponent> rootCategories = MainGuiApp.categoryService.getAllRootCategories();
        if (rootCategories.isEmpty()) {
            System.out.println("Warning: No categories loaded. Consider initializing them.");
        }

        TreeItem<CategoryComponent> rootItem = new TreeItem<>(new Category("Всі категорії"));
        rootItem.setExpanded(true);

        for (CategoryComponent rootCategory : rootCategories) {
            rootItem.getChildren().add(createTreeItem(rootCategory));
        }
        categoryTreeView.setRoot(rootItem);
        categoryTreeView.setShowRoot(false);

        categoryTreeView.setCellFactory(tv -> new TreeCell<CategoryComponent>() {
            @Override
            protected void updateItem(CategoryComponent item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    private TreeItem<CategoryComponent> createTreeItem(CategoryComponent categoryComponent) {
        TreeItem<CategoryComponent> item = new TreeItem<>(categoryComponent);
        item.setExpanded(true);
        if (categoryComponent instanceof Category) {
            Category category = (Category) categoryComponent;
            for (CategoryComponent subCategory : category.getSubCategories()) {
                item.getChildren().add(createTreeItem(subCategory));
            }
        }
        return item;
    }

    private void setupAdListView() {
        adListView.setItems(adsObservableList);

        adListView.setCellFactory(new Callback<ListView<Ad>, ListCell<Ad>>() {
            @Override
            public ListCell<Ad> call(ListView<Ad> listView) {
                return new ListCell<Ad>() {
                    private final VBox contentBox = new VBox(5);
                    private final Label titleLabel = new Label();
                    private final Label priceLabel = new Label();
                    private final Label categoryLabel = new Label();
                    private final Label sellerLabel = new Label();
                    private final Label statusLabel = new Label();
                    private final Text descriptionText = new Text();
                    private final HBox actionButtons = new HBox(5);

                    {
                        titleLabel.getStyleClass().add("ad-title-in-list");
                        priceLabel.getStyleClass().add("ad-price-in-list");
                        categoryLabel.getStyleClass().add("ad-category-in-list");
                        sellerLabel.getStyleClass().add("ad-category-in-list");
                        statusLabel.getStyleClass().add("ad-status-in-list");
                        descriptionText.setWrappingWidth(300);

                        contentBox.getChildren().addAll(titleLabel, priceLabel, categoryLabel,
                                sellerLabel, statusLabel, descriptionText, actionButtons);
                    }

                    @Override
                    protected void updateItem(Ad ad, boolean empty) {
                        super.updateItem(ad, empty);
                        if (empty || ad == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            titleLabel.setText(ad.getTitle());
                            priceLabel.setText(String.format("%.2f грн", ad.getPrice()));
                            statusLabel.setText("Статус: " + ad.getStatus());
                            descriptionText.setText(ad.getDescription() != null && ad.getDescription().length() > 100 ?
                                    ad.getDescription().substring(0, 100) + "..." : ad.getDescription());

                            // Отримати ім'я категорії
                            Optional<CategoryComponent> catOpt = MainGuiApp.categoryService.findCategoryById(ad.getCategoryId());
                            categoryLabel.setText("Категорія: " + catOpt.map(CategoryComponent::getName).orElse("N/A"));

                            // Отримати ім'я продавця
                            try {
                                User seller = MainGuiApp.userService.getUserById(ad.getSellerId());
                                sellerLabel.setText("Продавець: " + seller.getUsername());
                            } catch (Exception e) {
                                sellerLabel.setText("Продавець: невідомий");
                            }

                            // Додаємо кнопки дій для власних оголошень
                            setupActionButtons(ad, actionButtons);
                            setGraphic(contentBox);
                        }
                    }
                };
            }
        });

        // Обробник подвійного кліку
        adListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Ad selectedAd = adListView.getSelectionModel().getSelectedItem();
                if (selectedAd != null) {
                    try {
                        MainGuiApp.loadAdDetailScene(selectedAd);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showErrorAlert("Помилка завантаження", "Не вдалося відкрити деталі оголошення.", e.getMessage());
                    }
                }
            }
        });
    }

    private void setupActionButtons(Ad ad, HBox actionButtons) {
        actionButtons.getChildren().clear();

        User currentUser = GlobalContext.getInstance().getLoggedInUser();
        if (currentUser != null && ad.getSellerId().equals(currentUser.getUserId())) {
            // Кнопки для власних оголошень

            Button editButton = new Button("Редагувати");
            editButton.setOnAction(e -> handleEditAd(ad));

            Button deleteButton = new Button("Видалити");
            deleteButton.setOnAction(e -> handleDeleteAd(ad));

            Button publishButton = new Button("Опублікувати");
            publishButton.setDisable(!ad.getStatus().equals("Чернетка"));
            publishButton.setOnAction(e -> handlePublishAd(ad));

            Button archiveButton = new Button("Архівувати");
            archiveButton.setDisable(ad.getStatus().equals("Архівоване"));
            archiveButton.setOnAction(e -> handleArchiveAd(ad));

            Button markSoldButton = new Button("Продано");
            markSoldButton.setDisable(ad.getStatus().equals("Продано"));
            markSoldButton.setOnAction(e -> handleMarkAsSold(ad));

            actionButtons.getChildren().addAll(editButton, deleteButton, publishButton, archiveButton, markSoldButton);
        }
    }

    private void handleEditAd(Ad ad) {
        try {
            MainGuiApp.loadEditAdScene(ad);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка", "Не вдалося завантажити форму редагування.", e.getMessage());
        }
    }

    private void handleDeleteAd(Ad ad) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Підтвердження видалення");
        confirmAlert.setHeaderText("Видалити оголошення?");
        confirmAlert.setContentText("Ви впевнені, що хочете видалити оголошення \"" + ad.getTitle() + "\"?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                User currentUser = GlobalContext.getInstance().getLoggedInUser();
                commandManager.deleteAd(ad.getAdId(), currentUser.getUserId());

                refreshCurrentView();
                updateCommandButtons();
                showInfoAlert("Успіх", "Оголошення успішно видалено!");

            } catch (UserNotFoundException e) {
                showErrorAlert("Помилка", "Не вдалося видалити оголошення.", e.getMessage());
            }
        }
    }

    private void handlePublishAd(Ad ad) {
        try {
            commandManager.publishAd(ad.getAdId());

            refreshCurrentView();
            updateCommandButtons();
            showInfoAlert("Успіх", "Оголошення успішно опубліковано!");

        } catch (UserNotFoundException e) {
            showErrorAlert("Помилка", "Не вдалося опублікувати оголошення.", e.getMessage());
        }
    }

    private void handleArchiveAd(Ad ad) {
        try {
            commandManager.archiveAd(ad.getAdId());

            refreshCurrentView();
            updateCommandButtons();
            showInfoAlert("Успіх", "Оголошення успішно архівовано!");

        } catch (UserNotFoundException e) {
            showErrorAlert("Помилка", "Не вдалося архівувати оголошення.", e.getMessage());
        }
    }

    private void handleMarkAsSold(Ad ad) {
        try {
            commandManager.markAsSold(ad.getAdId());

            refreshCurrentView();
            updateCommandButtons();
            showInfoAlert("Успіх", "Оголошення позначено як продане!");

        } catch (UserNotFoundException e) {
            showErrorAlert("Помилка", "Не вдалося позначити оголошення як продане.", e.getMessage());
        }
    }

    @FXML
    private void handleUndo() {
        try {
            commandManager.undo();
            refreshCurrentView();
            updateCommandButtons();
            showInfoAlert("Успіх", "Команда успішно скасована!");

        } catch (UserNotFoundException e) {
            showErrorAlert("Помилка", "Не вдалося скасувати команду.", e.getMessage());
        }
    }

    @FXML
    private void handleRedo() {
        try {
            commandManager.redo();
            refreshCurrentView();
            updateCommandButtons();
            showInfoAlert("Успіх", "Команда успішно повторена!");

        } catch (UserNotFoundException e) {
            showErrorAlert("Помилка", "Не вдалося повторити команду.", e.getMessage());
        }
    }

    @FXML
    private void handleClearHistory() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Підтвердження очищення");
        confirmAlert.setHeaderText("Очистити історію команд?");
        confirmAlert.setContentText("Це дія незворотна. Ви не зможете скасовувати або повторювати команди.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            commandManager.clearHistory();
            updateCommandButtons();
            showInfoAlert("Успіх", "Історія команд очищена!");
        }
    }

    private void refreshCurrentView() {
        loadAds(currentSelectedCategoryId);
    }

    private void loadAds(String categoryId) {
        List<Ad> ads;
        if (categoryId != null && !categoryId.isEmpty()) {
            ads = MainGuiApp.adService.getAdsByCategoryId(categoryId);
        } else {
            ads = MainGuiApp.adService.getAllAds();
        }
        adsObservableList.setAll(ads);
        if (ads.isEmpty()){
            System.out.println("No ads found for categoryId: " + categoryId);
        }
    }

    @FXML
    private void handleSearchAds() {
        String keyword = searchField.getText();
        List<Ad> searchResult = MainGuiApp.adService.searchAds(keyword, null, null, currentSelectedCategoryId);
        adsObservableList.setAll(searchResult);

        if(keyword.isEmpty() && currentSelectedCategoryId == null) {
            currentCategoryLabel.setText("Всі оголошення");
        } else if (keyword.isEmpty() && currentSelectedCategoryId != null) {
            // currentCategoryLabel вже має бути встановлено
        } else {
            currentCategoryLabel.setText("Результати пошуку для: \"" + keyword + "\" " +
                    (currentSelectedCategoryId != null ? "в категорії " + categoryTreeView.getSelectionModel().getSelectedItem().getValue().getName() : ""));
        }
    }

    @FXML
    private void handleCreateAd() {
        try {
            MainGuiApp.loadCreateAdScene();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка", "Не вдалося завантажити форму створення оголошення.", e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Підтвердження виходу");
        confirmAlert.setHeaderText("Ви впевнені, що хочете вийти?");
        confirmAlert.setContentText("Всі незбережені дані будуть втрачені.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            GlobalContext.getInstance().clearLoggedInUser();

            try {
                MainGuiApp.loadLoginScene();
            } catch (IOException e) {
                e.printStackTrace();
                Platform.exit();
            }
        }
    }

    @FXML
    private void handleExitApplication() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Підтвердження виходу");
        confirmAlert.setHeaderText("Ви впевнені, що хочете закрити програму?");
        confirmAlert.setContentText("Всі незбережені дані будуть втрачені.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
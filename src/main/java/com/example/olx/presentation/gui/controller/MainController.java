package com.example.olx.presentation.gui.controller;

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
    @FXML private HBox paginationControls; // Для майбутньої пагінації

    private ObservableList<Ad> adsObservableList = FXCollections.observableArrayList();
    private String currentSelectedCategoryId = null;

    @FXML
    public void initialize() {
        User currentUser = GlobalContext.getInstance().getLoggedInUser();
        if (currentUser != null) {
            loggedInUserLabel.setText("Користувач: " + currentUser.getUsername());
            // Всі функції доступні для авторизованого користувача
            createAdButton.setDisable(false);
            logoutButton.setDisable(false);
        } else {
            // Якщо користувач не авторизований, перенаправляємо на сторінку входу
            try {
                MainGuiApp.loadLoginScene();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Platform.exit(); // Закриваємо програму у випадку помилки
            }
        }

        setupCategoryTree();
        setupAdListView();
        loadAds(null); // Завантажити всі оголошення при старті

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

    private void setupCategoryTree() {
        List<CategoryComponent> rootCategories = MainGuiApp.categoryService.getAllRootCategories();
        if (rootCategories.isEmpty()) {
            System.out.println("Warning: No categories loaded. Consider initializing them.");
        }

        TreeItem<CategoryComponent> rootItem = new TreeItem<>(new Category("Всі категорії")); // Фіктивний корінь
        rootItem.setExpanded(true);

        for (CategoryComponent rootCategory : rootCategories) {
            rootItem.getChildren().add(createTreeItem(rootCategory));
        }
        categoryTreeView.setRoot(rootItem);
        categoryTreeView.setShowRoot(false); // Не показувати фіктивний корінь "Всі категорії"

        // Кастомне відображення тексту для елементів дерева
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
        item.setExpanded(true); // Розгортаємо всі категорії за замовчуванням
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
        // Кастомне відображення елементів списку (створюємо "картку" оголошення)
        adListView.setCellFactory(new Callback<ListView<Ad>, ListCell<Ad>>() {
            @Override
            public ListCell<Ad> call(ListView<Ad> listView) {
                return new ListCell<Ad>() {
                    private final VBox contentBox = new VBox(5); // Відступи між елементами
                    private final Label titleLabel = new Label();
                    private final Label priceLabel = new Label();
                    private final Label categoryLabel = new Label();
                    private final Label sellerLabel = new Label();
                    private final Text descriptionText = new Text(); // Для переносу тексту

                    {
                        titleLabel.getStyleClass().add("ad-title-in-list");
                        priceLabel.getStyleClass().add("ad-price-in-list");
                        categoryLabel.getStyleClass().add("ad-category-in-list");
                        sellerLabel.getStyleClass().add("ad-category-in-list"); // Схожий стиль
                        descriptionText.setWrappingWidth(300); // Обмеження ширини для переносу

                        contentBox.getChildren().addAll(titleLabel, priceLabel, categoryLabel, sellerLabel, descriptionText);
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
                            setGraphic(contentBox);
                        }
                    }
                };
            }
        });

        // Обробник подвійного кліку на оголошенні для відкриття деталей
        adListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Ad selectedAd = adListView.getSelectionModel().getSelectedItem();
                if (selectedAd != null) {
                    try {
                        MainGuiApp.loadAdDetailScene(selectedAd);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Помилка завантаження");
                        alert.setHeaderText("Не вдалося відкрити деталі оголошення.");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    }
                }
            }
        });
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка");
            alert.setHeaderText("Не вдалося завантажити форму створення оголошення.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleLogout() {
        // Показуємо підтвердження
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Підтвердження виходу");
        confirmAlert.setHeaderText("Ви впевнені, що хочете вийти?");
        confirmAlert.setContentText("Всі незбережені дані будуть втрачені.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Очищуємо дані користувача
            GlobalContext.getInstance().clearLoggedInUser();

            try {
                MainGuiApp.loadLoginScene();
            } catch (IOException e) {
                e.printStackTrace();
                // У випадку помилки завантаження сцени входу, закриваємо програму
                Platform.exit();
            }
        }
    }

    @FXML
    private void handleExitApplication() {
        // Показуємо підтвердження
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Підтвердження виходу");
        confirmAlert.setHeaderText("Ви впевнені, що хочете закрити програму?");
        confirmAlert.setContentText("Всі незбережені дані будуть втрачені.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }
}
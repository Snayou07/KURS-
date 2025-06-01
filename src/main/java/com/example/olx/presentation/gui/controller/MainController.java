package com.example.olx.presentation.gui.controller;

import com.example.olx.presentation.gui.mediator.components.SearchComponent;
import com.example.olx.presentation.gui.mediator.components.AdListComponent;
import com.example.olx.presentation.gui.mediator.components.FilterComponent;
import com.example.olx.application.command.AdCommandManager;
import com.example.olx.application.command.CommandFactory;
import com.example.olx.application.command.CommandInvoker;
import com.example.olx.domain.decorator.AdComponent;
import com.example.olx.domain.decorator.AdDecoratorFactory;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.Category;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.model.User;
import com.example.olx.presentation.gui.MainGuiApp;
import com.example.olx.presentation.gui.util.GlobalContext;

// Додаємо імпорти для медіатора
import com.example.olx.presentation.gui.mediator.AdBrowserMediator;
import com.example.olx.presentation.gui.mediator.components.*;

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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML private BorderPane mainBorderPane;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button createAdButton;
    @FXML private Label loggedInUserLabel;
    @FXML private Button logoutButton;
    @FXML private Button exitButton;
    @FXML private TreeView<CategoryComponent> categoryTreeView;
    @FXML private Label currentCategoryLabel;
    @FXML private ListView<AdComponent> adListView;
    @FXML private HBox paginationControls;

    // Розширений пошук
    @FXML private Button advancedSearchButton;
    @FXML private HBox advancedSearchPanel;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private CheckBox premiumOnlyCheckBox;
    @FXML private CheckBox urgentOnlyCheckBox;
    @FXML private Button applyFiltersButton;
    @FXML private Button clearFiltersButton;

    // Швидкі фільтри
    @FXML private CheckBox quickFilterPremium;
    @FXML private CheckBox quickFilterUrgent;
    @FXML private CheckBox quickFilterWithDelivery;
    @FXML private CheckBox quickFilterWithWarranty;
    @FXML private CheckBox quickFilterWithDiscount;

    // Command pattern components
    @FXML private Button undoButton;
    @FXML private Button redoButton;
    @FXML private Button clearHistoryButton;
    @FXML private ListView<String> commandHistoryListView;

    // Сортування та відображення
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button sortOrderButton;
    @FXML private Button listViewButton;
    @FXML private Button gridViewButton;
    @FXML private Button refreshButton;

    // Активні фільтри
    @FXML private HBox activeFiltersPanel;
    @FXML private ScrollPane activeFiltersScrollPane;
    @FXML private HBox activeFiltersContainer;
    @FXML private Button clearAllFiltersButton;

    // Пагінація
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private ComboBox<Integer> pageSizeComboBox;

    // Статистика
    @FXML private Label totalAdsLabel;
    @FXML private Label filteredAdsLabel;
    @FXML private Label selectedCategoryLabel;

    // Статус бар
    @FXML private Label statusLabel;
    @FXML private Label lastUpdateLabel;
    @FXML private Label mediatorStatusLabel;
    @FXML private HBox loadingIndicator;
    @FXML private Label loadingLabel;

    private AdCommandManager commandManager;
    private ObservableList<AdComponent> adsObservableList = FXCollections.observableArrayList();
    private ObservableList<String> commandHistoryObservableList = FXCollections.observableArrayList();
    private String currentSelectedCategoryId = null;
    private Random random = new Random();

    // Додаємо компоненти медіатора
    private AdBrowserMediator mediator;
    private SearchComponent searchComponent;
    private AdListComponent adListComponent;
    private FilterComponent filterComponent;

    // Додаткові змінні для пагінації та сортування
    private int currentPage = 1;
    private int pageSize = 20;
    private boolean isAscendingSort = true;
    private String currentSortBy = "title";
    private boolean isAdvancedSearchVisible = false;

    @FXML
    public void initialize() {
        System.out.println("MainController initialization started...");

        // Перевіряємо поточного користувача
        User currentUser = GlobalContext.getInstance().getLoggedInUser();
        if (currentUser != null) {
            loggedInUserLabel.setText("Користувач: " + currentUser.getUsername());
            if (createAdButton != null) createAdButton.setDisable(false);
            if (logoutButton != null) logoutButton.setDisable(false);
        } else {
            try {
                MainGuiApp.loadLoginScene();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Platform.exit();
            }
        }

        // Перевіряємо доступність сервісів
        if (MainGuiApp.adService == null) {
            System.err.println("ERROR: AdService is null!");
            showErrorAlert("Помилка ініціалізації", "Сервіс оголошень не доступний", "Перезапустіть програму");
            return;
        }

        if (MainGuiApp.categoryService == null) {
            System.err.println("ERROR: CategoryService is null!");
            showErrorAlert("Помилка ініціалізації", "Сервіс категорій не доступний", "Перезапустіть програму");
            return;
        }

        try {
            initializeCommandManager();
            initializeMediator();
            initializeUIComponents();
            setupCategoryTree();
            setupAdListView();
            setupCommandHistoryView();
            setupMediatorIntegration();
            loadAds(null);
            updateCommandButtons();
            updateStatistics();
            updateLastUpdateTime();

            // Обробник вибору категорії в дереві
            if (categoryTreeView != null) {
                categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                        (observable, oldValue, newValue) -> handleCategorySelection(newValue));
            }

            System.out.println("MainController initialization completed successfully.");

        } catch (Exception e) {
            System.err.println("Error during MainController initialization: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Помилка ініціалізації", "Не вдалося ініціалізувати головне вікно", e.getMessage());
        }
    }

    /**
     * Обробка вибору категорії
     */
    private void handleCategorySelection(TreeItem<CategoryComponent> newValue) {
        try {
            if (newValue != null && newValue.getValue() != null) {
                CategoryComponent selectedCategory = newValue.getValue();
                currentSelectedCategoryId = selectedCategory.getId();

                String categoryNameSafe = selectedCategory.getName() != null ? selectedCategory.getName() : "Невідома категорія";

                if (currentCategoryLabel != null) {
                    currentCategoryLabel.setText("Оголошення в категорії: " + categoryNameSafe);
                }
                if (selectedCategoryLabel != null) {
                    selectedCategoryLabel.setText("Обрана категорія: " + categoryNameSafe);
                }

                // Оновлюємо компонент пошуку через медіатор
                if (searchComponent != null) {
                    searchComponent.updateCategory(selectedCategory.getId());
                }
                loadAds(selectedCategory.getId());
            } else {
                currentSelectedCategoryId = null;
                if (currentCategoryLabel != null) {
                    currentCategoryLabel.setText("Всі оголошення");
                }
                if (selectedCategoryLabel != null) {
                    selectedCategoryLabel.setText("Обрана категорія: немає");
                }

                // Очищуємо категорію в компоненті пошуку
                if (searchComponent != null) {
                    searchComponent.updateCategory("");
                }
                loadAds(null);
            }
        } catch (Exception e) {
            System.err.println("Error handling category selection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ініціалізація UI компонентів
     */
    private void initializeUIComponents() {
        try {
            // Ініціалізація комбо-боксів
            if (statusFilterCombo != null) {
                statusFilterCombo.getItems().addAll("Всі", "Активне", "Чернетка", "Архівоване", "Продано");
                statusFilterCombo.setValue("Всі");
            }

            if (sortComboBox != null) {
                sortComboBox.getItems().addAll("За назвою", "За ціною", "За датою", "За популярністю");
                sortComboBox.setValue("За назвою");
                sortComboBox.setOnAction(e -> handleSortChange());
            }

            if (pageSizeComboBox != null) {
                pageSizeComboBox.getItems().addAll(10, 20, 50, 100);
                pageSizeComboBox.setValue(20);
                pageSizeComboBox.setOnAction(e -> handlePageSizeChange());
            }

            // Ініціалізація швидких фільтрів
            setupQuickFilters();

            // Початкове приховування панелі розширеного пошуку
            if (advancedSearchPanel != null) {
                advancedSearchPanel.setVisible(false);
                advancedSearchPanel.setManaged(false);
            }

            System.out.println("UI components initialized successfully");

        } catch (Exception e) {
            System.err.println("Error initializing UI components: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Налаштування швидких фільтрів
     */
    private void setupQuickFilters() {
        try {
            if (quickFilterPremium != null) {
                quickFilterPremium.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters());
            }
            if (quickFilterUrgent != null) {
                quickFilterUrgent.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters());
            }
            if (quickFilterWithDelivery != null) {
                quickFilterWithDelivery.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters());
            }
            if (quickFilterWithWarranty != null) {
                quickFilterWithWarranty.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters());
            }
            if (quickFilterWithDiscount != null) {
                quickFilterWithDiscount.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters());
            }
        } catch (Exception e) {
            System.err.println("Error setting up quick filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Застосування швидких фільтрів
     */
    private void applyQuickFilters() {
        try {
            // Логіка фільтрації на основі вибраних швидких фільтрів
            refreshCurrentView();
            updateActiveFiltersDisplay();
        } catch (Exception e) {
            System.err.println("Error applying quick filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ініціалізація медіатора та його компонентів
     */
    private void initializeMediator() {
        try {
            // Створюємо медіатор
            mediator = new AdBrowserMediator(MainGuiApp.adService, MainGuiApp.categoryService);

            // Створюємо компоненти
            searchComponent = new SearchComponent(mediator);
            adListComponent = new AdListComponent(mediator);
            filterComponent = new FilterComponent(mediator);

            // Реєструємо компоненти в медіаторі
            mediator.registerComponents(searchComponent, adListComponent, filterComponent);

            System.out.println("Медіатор ініціалізовано успішно");
            updateMediatorStatus("активний");

        } catch (Exception e) {
            System.err.println("Error initializing mediator: " + e.getMessage());
            e.printStackTrace();
            updateMediatorStatus("помилка");
        }
    }

    /**
     * Інтеграція медіатора з існуючими UI елементами
     */
    private void setupMediatorIntegration() {
        try {
            // Інтегруємо пошукове поле з медіатором
            if (searchField != null && searchComponent != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (searchComponent != null) {
                        searchComponent.updateSearchText(newValue);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error setting up mediator integration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== ОБРОБНИКИ ПОДІЙ ==========

    /**
     * Перемикання видимості панелі розширеного пошуку
     */
    @FXML
    private void handleToggleAdvancedSearch() {
        try {
            isAdvancedSearchVisible = !isAdvancedSearchVisible;
            if (advancedSearchPanel != null) {
                advancedSearchPanel.setVisible(isAdvancedSearchVisible);
                advancedSearchPanel.setManaged(isAdvancedSearchVisible);
            }
            updateStatus("Розширений пошук " + (isAdvancedSearchVisible ? "відкрито" : "закрито"));
        } catch (Exception e) {
            System.err.println("Error toggling advanced search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Застосування фільтрів з панелі розширеного пошуку
     */
    @FXML
    private void handleApplyFilters() {
        try {
            showLoadingIndicator("Застосування фільтрів...");

            // Отримуємо значення фільтрів
            String minPriceText = minPriceField != null ? minPriceField.getText() : "";
            String maxPriceText = maxPriceField != null ? maxPriceField.getText() : "";
            String selectedStatus = statusFilterCombo != null ? statusFilterCombo.getValue() : "Всі";
            boolean premiumOnly = premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected();
            boolean urgentOnly = urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected();

            // Парсимо цінові фільтри
            Double minPrice = null;
            Double maxPrice = null;

            try {
                if (!minPriceText.isEmpty()) {
                    minPrice = Double.parseDouble(minPriceText);
                }
                if (!maxPriceText.isEmpty()) {
                    maxPrice = Double.parseDouble(maxPriceText);
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Помилка фільтрації", "Невірний формат ціни", "Будь ласка, введіть коректні числові значення для ціни.");
                hideLoadingIndicator();
                return;
            }

            // Застосовуємо фільтри через сервіс
            String keyword = searchField != null ? searchField.getText() : "";
            List<Ad> filteredAds = MainGuiApp.adService.searchAds(keyword, minPrice, maxPrice, currentSelectedCategoryId);

            // Додаткова фільтрація за статусом та спеціальними атрибутами
            if (!"Всі".equals(selectedStatus)) {
                filteredAds = filteredAds.stream()
                        .filter(ad -> selectedStatus.equals(ad.getStatus()))
                        .toList();
            }

            List<AdComponent> decoratedAds = filteredAds.stream()
                    .map(this::createDecoratedAd)
                    .filter(adComponent -> {
                        if (premiumOnly && !adComponent.getDisplayInfo().contains("⭐ ПРЕМІУМ")) {
                            return false;
                        }
                        if (urgentOnly && !adComponent.getDisplayInfo().contains("🚨 ТЕРМІНОВО")) {
                            return false;
                        }
                        return true;
                    })
                    .toList();

            adsObservableList.setAll(decoratedAds);
            updateActiveFiltersDisplay();
            updateStatistics();
            hideLoadingIndicator();
            updateStatus("Фільтри застосовано. Знайдено " + decoratedAds.size() + " оголошень");

        } catch (Exception e) {
            System.err.println("Error applying filters: " + e.getMessage());
            e.printStackTrace();
            hideLoadingIndicator();
            showErrorAlert("Помилка", "Не вдалося застосувати фільтри", e.getMessage());
        }
    }

    /**
     * Очищення фільтрів розширеного пошуку
     */
    @FXML
    private void handleClearFilters() {
        try {
            if (minPriceField != null) minPriceField.clear();
            if (maxPriceField != null) maxPriceField.clear();
            if (statusFilterCombo != null) statusFilterCombo.setValue("Всі");
            if (premiumOnlyCheckBox != null) premiumOnlyCheckBox.setSelected(false);
            if (urgentOnlyCheckBox != null) urgentOnlyCheckBox.setSelected(false);

            updateActiveFiltersDisplay();
            refreshCurrentView();
            updateStatus("Фільтри очищено");
        } catch (Exception e) {
            System.err.println("Error clearing filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Очищення всіх фільтрів (включно зі швидкими)
     */
    @FXML
    private void handleClearAllFilters() {
        try {
            // Очищуємо розширені фільтри
            handleClearFilters();

            // Очищуємо швидкі фільтри
            if (quickFilterPremium != null) quickFilterPremium.setSelected(false);
            if (quickFilterUrgent != null) quickFilterUrgent.setSelected(false);
            if (quickFilterWithDelivery != null) quickFilterWithDelivery.setSelected(false);
            if (quickFilterWithWarranty != null) quickFilterWithWarranty.setSelected(false);
            if (quickFilterWithDiscount != null) quickFilterWithDiscount.setSelected(false);

            updateActiveFiltersDisplay();
            refreshCurrentView();
            updateStatus("Всі фільтри очищено");
        } catch (Exception e) {
            System.err.println("Error clearing all filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== МЕТОДИ НАЛАШТУВАННЯ КАТЕГОРІЙ (ВИПРАВЛЕНІ) ==========

    private void setupCategoryTree() {
        System.out.println("Setting up category tree...");

        try {
            // Перевіряємо доступність сервісу категорій
            if (MainGuiApp.categoryService == null) {
                System.err.println("ERROR: CategoryService is null in setupCategoryTree!");
                showErrorAlert("Помилка", "Сервіс категорій недоступний", "Перезапустіть програму");
                return;
            }

            // Отримуємо категорії з перевіркою на null
            List<CategoryComponent> rootCategories = null;
            try {
                rootCategories = MainGuiApp.categoryService.getAllRootCategories();
                System.out.println("Retrieved " + (rootCategories != null ? rootCategories.size() : 0) + " root categories");
            } catch (Exception e) {
                System.err.println("Error getting root categories: " + e.getMessage());
                e.printStackTrace();
            }

            // Додаємо перевірку на null
            if (rootCategories == null) {
                rootCategories = new ArrayList<>();
                System.err.println("Warning: CategoryService.getAllRootCategories() returned null. Using empty list.");
            }

            if (rootCategories.isEmpty()) {
                System.out.println("Warning: No categories loaded. Creating test categories...");
                // Створюємо тестові категорії для демонстрації
                rootCategories = createTestCategories();
            }

            // Створюємо кореневу категорію
            Category allCategoriesRoot = new Category("root", "Всі категорії", null);
            TreeItem<CategoryComponent> rootItem = new TreeItem<>(allCategoriesRoot);
            rootItem.setExpanded(true);

            // Додаємо категорії до дерева з перевіркою на null
            for (CategoryComponent rootCategory : rootCategories) {
                if (rootCategory != null) {
                    TreeItem<CategoryComponent> categoryItem = createTreeItem(rootCategory);
                    if (categoryItem != null) {
                        rootItem.getChildren().add(categoryItem);
                        System.out.println("Added category: " + rootCategory.getName());
                    }
                } else {
                    System.err.println("Warning: Found null root category, skipping...");
                }
            }

            // Встановлюємо дерево в UI
            if (categoryTreeView != null) {
                categoryTreeView.setRoot(rootItem);
                categoryTreeView.setShowRoot(false);

                categoryTreeView.setCellFactory(tv -> new TreeCell<CategoryComponent>() {
                    @Override
                    protected void updateItem(CategoryComponent item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            String name = item.getName();
                            setText(name != null ? name : "Невідома категорія");
                        }
                    }
                });

                System.out.println("Category tree setup completed successfully");
            } else {
                System.err.println("Error: categoryTreeView is null. Check FXML binding.");
            }

        } catch (Exception e) {
            System.err.println("Error setting up category tree: " + e.getMessage());
            e.printStackTrace();

            // Створюємо мінімальну структуру дерева як fallback
            createFallbackCategoryTree();
        }
    }

    /**
     * Створення тестових категорій для демонстрації
     */
    private List<CategoryComponent> createTestCategories() {
        List<CategoryComponent> testCategories = new ArrayList<>();

        try {
            // Створюємо основні категорії
            Category electronics = new Category("electronics", "Електроніка", null);
            Category vehicles = new Category("vehicles", "Транспорт", null);
            Category realestate = new Category("realestate", "Нерухомість", null);
            Category clothing = new Category("clothing", "Одяг", null);

            // Додаємо підкategорії для електроніки
            Category phones = new Category("phones", "Телефони", "electronics");
            Category computers = new Category("computers", "Комп'ютери", "electronics");
            electronics.addChild(phones);
            electronics.addChild(computers);

            // Додаємо підкategорії для транспорту
            Category cars = new Category("cars", "Автомобілі", "vehicles");
            Category motorcycles = new Category("motorcycles", "Мотоцикли", "vehicles");
            vehicles.addChild(cars);
            vehicles.addChild(motorcycles);

            testCategories.add(electronics);
            testCategories.add(vehicles);
            testCategories.add(realestate);
            testCategories.add(clothing);

            System.out.println("Created " + testCategories.size() + " test categories");

        } catch (Exception e) {
            System.err.println("Error creating test categories: " + e.getMessage());
            e.printStackTrace();
        }

        return testCategories;
    }

    /**
     * Створення fallback дерева категорій
     */
    private void createFallbackCategoryTree() {
        try {
            Category fallbackRoot = new Category("root", "Всі категорії", null);
            TreeItem<CategoryComponent> fallbackItem = new TreeItem<>(fallbackRoot);
            fallbackItem.setExpanded(true);

            if (categoryTreeView != null) {
                categoryTreeView.setRoot(fallbackItem);
                categoryTreeView.setShowRoot(false);
                System.out.println("Fallback category tree created");
            }
        } catch (Exception e) {
            System.err.println("Error creating fallback category tree: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Створення елемента дерева категорій (виправлений метод)
     */
    private TreeItem<CategoryComponent> createTreeItem(CategoryComponent categoryComponent) {
        if (categoryComponent == null) {
            System.err.println("Warning: Attempting to create TreeItem for null category");
            return null;
        }

        try {
            TreeItem<CategoryComponent> item = new TreeItem<>(categoryComponent);
            item.setExpanded(true);

            // Перевіряємо чи це Category та чи має дітей
            if (categoryComponent instanceof Category) {
                Category category = (Category) categoryComponent;

                // Безпечно отримуємо дітей
                CategoryComponent[] childrenArray = category.getChildren();
                if (childrenArray != null && childrenArray.length > 0) {
                    List<CategoryComponent> children = Arrays.asList(childrenArray);

                    for (CategoryComponent child : children) {
                        if (child != null) {
                            TreeItem<CategoryComponent> childItem = createTreeItem(child);
                            if (childItem != null) {
                                item.getChildren().add(childItem);
                            }
                        } else {
                            System.err.println("Warning: Found null child category in category: " +
                                    (category.getName() != null ? category.getName() : "Unknown"));
                        }
                    }
                }
            }

            return item;

        } catch (Exception e) {
            System.err.println("Error creating TreeItem for category: " +
                    (categoryComponent.getName() != null ? categoryComponent.getName() : "Unknown"));
            e.printStackTrace();
            return null;
        }
    }

    // ========== РЕШTA МЕТОДІВ (ЗАЛИШАЄТЬСЯ БЕЗ ЗМІН, АЛЕ З ДОДАТКОВИМИ ПЕРЕВІРКАМИ) ==========

    private void setupAdListView() {
        if (adListView != null) {
            adListView.setItems(adsObservableList);
            adListView.setCellFactory(new Callback<ListView<AdComponent>, ListCell<AdComponent>>() {
                @Override
                public ListCell<AdComponent> call(ListView<AdComponent> param) {
                    return new ListCell<AdComponent>() {
                        @Override
                        protected void updateItem(AdComponent item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                setText(item.getDisplayInfo());
                                setOnMouseClicked(event -> {
                                    if (event.getClickCount() == 2) {
                                        handleOpenAdDetails(item.getAd());
                                    }
                                });
                            }
                        }
                    };
                }
            });
        }
    }
    private void validateCategoryData() {
        try {
            List<CategoryComponent> rootCategories = MainGuiApp.categoryService.getAllRootCategories();

            if (rootCategories == null) {
                System.err.println("VALIDATION ERROR: getAllRootCategories() returned null");
                return;
            }

            System.out.println("Validating " + rootCategories.size() + " root categories...");

            for (int i = 0; i < rootCategories.size(); i++) {
                CategoryComponent category = rootCategories.get(i);
                if (category == null) {
                    System.err.println("VALIDATION ERROR: Root category at index " + i + " is null");
                    continue;
                }

                validateCategory(category, "Root[" + i + "]");
            }

            System.out.println("Category validation completed.");

        } catch (Exception e) {
            System.err.println("Error during category validation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateCategory(CategoryComponent category, String path) {
        if (category == null) {
            System.err.println("VALIDATION ERROR: Category is null at path: " + path);
            return;
        }

        String name = category.getName();
        String id = category.getId();

        if (name == null) {
            System.err.println("VALIDATION WARNING: Category name is null at path: " + path);
        }

        if (id == null) {
            System.err.println("VALIDATION WARNING: Category id is null at path: " + path);
        }

        if (category instanceof Category) {
            Category cat = (Category) category;
            List<CategoryComponent> children = List.of(cat.getChildren());

            if (children == null) {
                System.out.println("INFO: Category '" + name + "' has null children list");
            } else {
                for (int i = 0; i < children.size(); i++) {
                    CategoryComponent child = children.get(i);
                    validateCategory(child, path + " -> " + (name != null ? name : "null") + "[" + i + "]");
                }
            }
        }
    }
    private void loadAds(String categoryId) {
        showLoadingIndicator("Завантаження оголошень...");

        List<Ad> ads;
        if (categoryId != null && !categoryId.isEmpty()) {
            ads = MainGuiApp.adService.getAdsByCategoryId(categoryId);
        } else {
            ads = MainGuiApp.adService.getAllAds();
        }

        List<AdComponent> decoratedAds = ads.stream()
                .map(this::createDecoratedAd)
                .toList();

        adsObservableList.setAll(decoratedAds);
        applySorting();
        updateStatistics();
        updatePagination();
        hideLoadingIndicator();
        updateStatus("Завантажено " + decoratedAds.size() + " оголошень");
    }



    private void refreshCurrentView() {
        loadAds(currentSelectedCategoryId);
    }

    private void handleOpenAdDetails(Ad ad) {
        try {
            MainGuiApp.loadAdDetailScene(ad);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка", "Не вдалося відкрити деталі оголошення", e.getMessage());
        }
    }

    @FXML
    private void handleSearchAds() {
        String keyword = searchField.getText();
        String categoryId = currentSelectedCategoryId;

        showLoadingIndicator("Пошук...");

        List<Ad> searchResults = MainGuiApp.adService.searchAds(keyword, null, null, categoryId);
        List<AdComponent> decoratedResults = searchResults.stream()
                .map(this::createDecoratedAd)
                .toList();

        adsObservableList.setAll(decoratedResults);
        updateStatistics();
        updatePagination();
        hideLoadingIndicator();
        updateStatus("Знайдено " + decoratedResults.size() + " оголошень за запитом: " + keyword);

        // Логування команди через медіатор
        if (searchComponent != null) {
            searchComponent.performSearch(keyword, categoryId);
        }
    }

    @FXML
    private void handleCreateAd() {
        try {
            MainGuiApp.loadCreateAdScene();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка", "Не вдалося відкрити форму створення оголошення", e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        GlobalContext.getInstance().setLoggedInUser(null);
        try {
            MainGuiApp.loadLoginScene();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @FXML
    private void handleExitApplication() {
        Optional<ButtonType> result = showConfirmationAlert(
                "Підтвердження виходу",
                "Ви впевнені, що хочете закрити програму?",
                "Всі незбережені зміни будуть втрачені."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    // ========== COMMAND PATTERN HANDLERS ==========

    @FXML
    private void handleUndo() throws UserNotFoundException {
        if (commandManager.canUndo()) {
            commandManager.undo();
            refreshCurrentView();
            updateCommandButtons();
            updateStatus("Команда скасована");
        }
    }

    @FXML
    private void handleRedo() throws UserNotFoundException {
        if (commandManager.canRedo()) {
            commandManager.redo();
            refreshCurrentView();
            updateCommandButtons();
            updateStatus("Команда повторена");
        }
    }

    @FXML
    private void handleClearHistory() {
        Optional<ButtonType> result = showConfirmationAlert(
                "Очистити історію команд",
                "Ви впевнені, що хочете очистити історію команд?",
                "Цю дію неможливо скасувати."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            commandManager.clearHistory();
            updateCommandButtons();
            updateStatus("Історія команд очищена");
        }
    }

    // ========== UTILITY METHODS ==========

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmationAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private AdComponent createDecoratedAd(Ad ad) {
        AdDecoratorFactory decoratorFactory = new AdDecoratorFactory();
        AdComponent baseComponent = decoratorFactory.createBaseAdComponent(ad);

        // Застосовуємо декоратори на основі властивостей оголошення
        if (ad.isPremium()) {
            baseComponent = decoratorFactory.createPremiumAd((Ad) baseComponent);
        }

        if (ad.isUrgent()) {
            baseComponent = decoratorFactory.createUrgentAd((Ad) baseComponent);
        }

        if (ad.hasDiscount()) {
            baseComponent = decoratorFactory.createDiscountAd(baseComponent);
        }

        return baseComponent;
    }
    // ========== MEDIATOR INTEGRATION METHODS ==========

    /**
     * Метод для оновлення списку оголошень через медіатор
     */
    public void updateAdsList(List<Ad> ads) {
        List<AdComponent> decoratedAds = ads.stream()
                .map(this::createDecoratedAd)
                .toList();

        Platform.runLater(() -> {
            adsObservableList.setAll(decoratedAds);
            applySorting();
            updateStatistics();
            updatePagination();
            updateStatus("Список оновлено через медіатор");
        });
    }

    /**
     * Метод для оновлення статусу через медіатор
     */
    public void updateMediatorMessage(String message) {
        Platform.runLater(() -> {
            updateStatus(message);
            updateMediatorStatus("активний");
        });
    }

    /**
     * Отримання поточного користувача для медіатора
     */
    public User getCurrentUser() {
        return GlobalContext.getInstance().getLoggedInUser();
    }

    /**
     * Метод для логування дій через медіатор
     */
    public void logMediatorAction(String action) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logMessage = "[" + timestamp + "] " + action;

        Platform.runLater(() -> {
            if (commandHistoryObservableList.size() > 50) {
                commandHistoryObservableList.remove(0);
            }
            commandHistoryObservableList.add(logMessage);

            // Прокручуємо до останнього елемента
            if (commandHistoryListView != null && !commandHistoryObservableList.isEmpty()) {
                commandHistoryListView.scrollTo(commandHistoryObservableList.size() - 1);
            }
        });
    }

    // ========== CLEANUP ==========

    /**
     * Метод очищення ресурсів при закритті контролера
     */
    public void cleanup() {
        if (mediator != null) {
            // Можна додати логіку очищення медіатора
            updateMediatorStatus("неактивний");
        }

        // Очищення слухачів подій
        if (categoryTreeView != null) {
            categoryTreeView.getSelectionModel().selectedItemProperty().removeListener(
                    (ChangeListener<TreeItem<CategoryComponent>>) null
            );
        }

        updateStatus("Контролер очищено");
    }
}
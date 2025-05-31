package com.example.olx.presentation.gui.controller;


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
        categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        CategoryComponent selectedCategory = newValue.getValue();
                        currentSelectedCategoryId = selectedCategory.getId();
                        currentCategoryLabel.setText("Оголошення в категорії: " + selectedCategory.getName());
                        selectedCategoryLabel.setText("Обрана категорія: " + selectedCategory.getName());


                        // Оновлюємо компонент пошуку через медіатор
                        searchComponent.updateCategory(selectedCategory.getId());
                        loadAds(selectedCategory.getId());
                    } else {
                        currentSelectedCategoryId = null;
                        currentCategoryLabel.setText("Всі оголошення");
                        selectedCategoryLabel.setText("Обрана категорія: немає");


                        // Очищуємо категорію в компоненті пошуку
                        searchComponent.updateCategory("");
                        loadAds(null);
                    }
                });
    }


    /**
     * Ініціалізація UI компонентів
     */
    private void initializeUIComponents() {
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
    }


    /**
     * Налаштування швидких фільтрів
     */
    private void setupQuickFilters() {
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
    }


    /**
     * Застосування швидких фільтрів
     */
    private void applyQuickFilters() {
        // Логіка фільтрації на основі вибраних швидких фільтрів
        refreshCurrentView();
        updateActiveFiltersDisplay();
    }


    /**
     * Ініціалізація медіатора та його компонентів
     */
    private void initializeMediator() {
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
    }


    /**
     * Інтеграція медіатора з існуючими UI елементами
     */
    private void setupMediatorIntegration() {
        // Інтегруємо пошукове поле з медіатором
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchComponent.updateSearchText(newValue);
        });
    }


    // ========== ОБРОБНИКИ ПОДІЙ ==========


    /**
     * Перемикання видимості панелі розширеного пошуку
     */
    @FXML
    private void handleToggleAdvancedSearch() {
        isAdvancedSearchVisible = !isAdvancedSearchVisible;
        if (advancedSearchPanel != null) {
            advancedSearchPanel.setVisible(isAdvancedSearchVisible);
            advancedSearchPanel.setManaged(isAdvancedSearchVisible);
        }
        updateStatus("Розширений пошук " + (isAdvancedSearchVisible ? "відкрито" : "закрито"));
    }


    /**
     * Застосування фільтрів з панелі розширеного пошуку
     */
    @FXML
    private void handleApplyFilters() {
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
        String keyword = searchField.getText();
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
    }


    /**
     * Очищення фільтрів розширеного пошуку
     */
    @FXML
    private void handleClearFilters() {
        if (minPriceField != null) minPriceField.clear();
        if (maxPriceField != null) maxPriceField.clear();
        if (statusFilterCombo != null) statusFilterCombo.setValue("Всі");
        if (premiumOnlyCheckBox != null) premiumOnlyCheckBox.setSelected(false);
        if (urgentOnlyCheckBox != null) urgentOnlyCheckBox.setSelected(false);


        updateActiveFiltersDisplay();
        refreshCurrentView();
        updateStatus("Фільтри очищено");
    }


    /**
     * Очищення всіх фільтрів (включно зі швидкими)
     */
    @FXML
    private void handleClearAllFilters() {
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
    }


    /**
     * Зміна порядку сортування
     */
    @FXML
    private void handleToggleSortOrder() {
        isAscendingSort = !isAscendingSort;
        if (sortOrderButton != null) {
            sortOrderButton.setText(isAscendingSort ? "↑" : "↓");
        }
        applySorting();
        updateStatus("Порядок сортування змінено на " + (isAscendingSort ? "зростаючий" : "спадаючий"));
    }


    /**
     * Зміна типу сортування
     */
    private void handleSortChange() {
        String selectedSort = sortComboBox.getValue();
        switch (selectedSort) {
            case "За назвою":
                currentSortBy = "title";
                break;
            case "За ціною":
                currentSortBy = "price";
                break;
            case "За датою":
                currentSortBy = "date";
                break;
            case "За популярністю":
                currentSortBy = "popularity";
                break;
        }
        applySorting();
        updateStatus("Сортування змінено на: " + selectedSort);
    }


    /**
     * Застосування сортування
     */
    private void applySorting() {
        ObservableList<AdComponent> sortedList = FXCollections.observableArrayList(adsObservableList);


        switch (currentSortBy) {
            case "title":
                sortedList.sort((a1, a2) -> isAscendingSort ?
                        a1.getAd().getTitle().compareToIgnoreCase(a2.getAd().getTitle()) :
                        a2.getAd().getTitle().compareToIgnoreCase(a1.getAd().getTitle()));
                break;
            case "price":
                sortedList.sort((a1, a2) -> isAscendingSort ?
                        Double.compare(a1.getCalculatedPrice(), a2.getCalculatedPrice()) :
                        Double.compare(a2.getCalculatedPrice(), a1.getCalculatedPrice()));
                break;
            // Додати інші варіанти сортування за потребою
        }


        adsObservableList.setAll(sortedList);
    }


    /**
     * Перемикання на список
     */
    @FXML
    private void handleSwitchToListView() {
        // Логіка перемикання на вигляд списку
        if (listViewButton != null) listViewButton.setStyle("-fx-background-color: #4CAF50;");
        if (gridViewButton != null) gridViewButton.setStyle("");
        updateStatus("Перемкнуто на вигляд списку");
    }


    /**
     * Перемикання на сітку
     */
    @FXML
    private void handleSwitchToGridView() {
        // Логіка перемикання на вигляд сітки
        if (gridViewButton != null) gridViewButton.setStyle("-fx-background-color: #4CAF50;");
        if (listViewButton != null) listViewButton.setStyle("");
        updateStatus("Перемкнуто на вигляд сітки");
    }


    /**
     * Оновлення списку
     */
    @FXML
    private void handleRefresh() {
        showLoadingIndicator("Оновлення...");
        refreshCurrentView();
        updateLastUpdateTime();
        hideLoadingIndicator();
        updateStatus("Список оновлено");
    }


    // ========== ПАГІНАЦІЯ ==========


    /**
     * Перша сторінка
     */
    @FXML
    private void handleFirstPage() {
        if (currentPage > 1) {
            currentPage = 1;
            updatePagination();
            updateStatus("Перехід на першу сторінку");
        }
    }


    /**
     * Попередня сторінка
     */
    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
            updateStatus("Перехід на попередню сторінку");
        }
    }


    /**
     * Наступна сторінка
     */
    @FXML
    private void handleNextPage() {
        int totalPages = getTotalPages();
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
            updateStatus("Перехід на наступну сторінку");
        }
    }


    /**
     * Остання сторінка
     */
    @FXML
    private void handleLastPage() {
        int totalPages = getTotalPages();
        if (currentPage < totalPages) {
            currentPage = totalPages;
            updatePagination();
            updateStatus("Перехід на останню сторінку");
        }
    }


    /**
     * Зміна розміру сторінки
     */
    private void handlePageSizeChange() {
        Integer newPageSize = pageSizeComboBox.getValue();
        if (newPageSize != null) {
            pageSize = newPageSize;
            currentPage = 1; // Повертаємося на першу сторінку
            updatePagination();
            updateStatus("Розмір сторінки змінено на " + pageSize);
        }
    }


    /**
     * Оновлення пагінації
     */
    private void updatePagination() {
        int totalPages = getTotalPages();


        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Сторінка " + currentPage + " з " + totalPages);
        }


        if (firstPageButton != null) firstPageButton.setDisable(currentPage <= 1);
        if (prevPageButton != null) prevPageButton.setDisable(currentPage <= 1);
        if (nextPageButton != null) nextPageButton.setDisable(currentPage >= totalPages);
        if (lastPageButton != null) lastPageButton.setDisable(currentPage >= totalPages);


        // Показуємо/приховуємо контроли пагінації
        if (paginationControls != null) {
            paginationControls.setVisible(totalPages > 1);
        }
    }


    /**
     * Отримання загальної кількості сторінок
     */
    private int getTotalPages() {
        int totalAds = adsObservableList.size();
        return Math.max(1, (int) Math.ceil((double) totalAds / pageSize));
    }


    // ========== ДОПОМІЖНІ МЕТОДИ ==========


    /**
     * Оновлення відображення активних фільтрів
     */
    private void updateActiveFiltersDisplay() {
        if (activeFiltersContainer == null || activeFiltersPanel == null) return;


        activeFiltersContainer.getChildren().clear();
        boolean hasActiveFilters = false;


        // Перевіряємо розширені фільтри
        if (minPriceField != null && !minPriceField.getText().isEmpty()) {
            addFilterChip("Мін. ціна: " + minPriceField.getText());
            hasActiveFilters = true;
        }


        if (maxPriceField != null && !maxPriceField.getText().isEmpty()) {
            addFilterChip("Макс. ціна: " + maxPriceField.getText());
            hasActiveFilters = true;
        }


        if (statusFilterCombo != null && !"Всі".equals(statusFilterCombo.getValue())) {
            addFilterChip("Статус: " + statusFilterCombo.getValue());
            hasActiveFilters = true;
        }


        if (premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected()) {
            addFilterChip("Тільки преміум");
            hasActiveFilters = true;
        }


        if (urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected()) {
            addFilterChip("Тільки терміново");
            hasActiveFilters = true;
        }


        // Перевіряємо швидкі фільтри
        if (quickFilterPremium != null && quickFilterPremium.isSelected()) {
            addFilterChip("⭐ Преміум");
            hasActiveFilters = true;
        }


        if (quickFilterUrgent != null && quickFilterUrgent.isSelected()) {
            addFilterChip("🚨 Терміново");
            hasActiveFilters = true;
        }


        if (quickFilterWithDelivery != null && quickFilterWithDelivery.isSelected()) {
            addFilterChip("🚚 З доставкою");
            hasActiveFilters = true;
        }


        if (quickFilterWithWarranty != null && quickFilterWithWarranty.isSelected()) {
            addFilterChip("🛡️ З гарантією");
            hasActiveFilters = true;
        }


        if (quickFilterWithDiscount != null && quickFilterWithDiscount.isSelected()) {
            addFilterChip("💰 Зі знижкою");
            hasActiveFilters = true;
        }


        activeFiltersPanel.setVisible(hasActiveFilters);
        activeFiltersPanel.setManaged(hasActiveFilters);
    }


    /**
     * Додавання чіпа фільтра
     */
    private void addFilterChip(String text) {
        Label filterChip = new Label(text);
        filterChip.getStyleClass().add("filter-chip");
        activeFiltersContainer.getChildren().add(filterChip);
    }


    /**
     * Оновлення статистики
     */
    private void updateStatistics() {
        if (totalAdsLabel != null) {
            int totalCount = MainGuiApp.adService.getAllAds().size();
            totalAdsLabel.setText("Всього оголошень: " + totalCount);
        }


        if (filteredAdsLabel != null) {
            filteredAdsLabel.setText("Після фільтрації: " + adsObservableList.size());
        }
    }


    /**
     * Оновлення статусу медіатора
     */
    private void updateMediatorStatus(String status) {
        if (mediatorStatusLabel != null) {
            mediatorStatusLabel.setText("Медіатор: " + status);
        }
    }


    /**
     * Оновлення часу останнього оновлення
     */
    private void updateLastUpdateTime() {
        if (lastUpdateLabel != null) {
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            lastUpdateLabel.setText("Останнє оновлення: " + currentTime);
        }
    }


    /**
     * Оновлення статусу в статус барі
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }


    /**
     * Показ індикатора завантаження
     */
    private void showLoadingIndicator(String message) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
            loadingIndicator.setManaged(true);
        }
        if (loadingLabel != null) {
            loadingLabel.setText(message);
        }
    }


    /**
     * Приховування індикатора завантаження
     */
    private void hideLoadingIndicator() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
            loadingIndicator.setManaged(false);
        }
    }


    // ========== ІСНУЮЧІ МЕТОДИ (БЕЗ ЗМІН) ==========


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
            for (CategoryComponent child : category.getChildren()) {
                item.getChildren().add(createTreeItem(child));
            }
        }
        return item;
    }

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

    private void loadAds(String categoryId) {
        showLoadingIndicator("Завантаження оголошень...");

        List<Ad> ads;
        if (categoryId != null && !categoryId.isEmpty()) {
            ads = MainGuiApp.adService.getAdsByCategory(categoryId);
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

    private AdComponent createDecoratedAd(Ad ad) {
        AdComponent adComponent = AdDecoratorFactory.createAdComponent(ad);

        // Додаємо випадкові декорації для демонстрації
        if (random.nextDouble() < 0.3) { // 30% шанс на преміум
            adComponent = AdDecoratorFactory.addPremiumDecoration(adComponent);
        }
        if (random.nextDouble() < 0.2) { // 20% шанс на терміново
            adComponent = AdDecoratorFactory.addUrgentDecoration(adComponent);
        }
        if (random.nextDouble() < 0.15) { // 15% шанс на знижку
            adComponent = AdDecoratorFactory.addDiscountDecoration(adComponent, 10 + random.nextInt(40));
        }
        if (random.nextDouble() < 0.25) { // 25% шанс на доставку
            adComponent = AdDecoratorFactory.addDeliveryDecoration(adComponent);
        }
        if (random.nextDouble() < 0.20) { // 20% шанс на гарантію
            adComponent = AdDecoratorFactory.addWarrantyDecoration(adComponent, 6 + random.nextInt(24));
        }

        return adComponent;
    }

    private void refreshCurrentView() {
        loadAds(currentSelectedCategoryId);
    }

    private void handleOpenAdDetails(Ad ad) {
        try {
            MainGuiApp.loadAdDetailsScene(ad);
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
    private void handleUndo() {
        if (commandManager.canUndo()) {
            commandManager.undo();
            refreshCurrentView();
            updateCommandButtons();
            updateStatus("Команда скасована");
        }
    }

    @FXML
    private void handleRedo() {
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
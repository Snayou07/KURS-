package com.example.olx.presentation.gui.controller;

import com.example.olx.domain.decorator.*;
import com.example.olx.presentation.gui.mediator.components.SearchComponent;
import com.example.olx.presentation.gui.mediator.components.AdListComponent;
import com.example.olx.presentation.gui.mediator.components.FilterComponent;
// import com.example.olx.application.command.AdCommandManager; // Removed
// import com.example.olx.application.command.CommandFactory; // Removed
// import com.example.olx.application.command.CommandInvoker; // Removed
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.Category;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.model.User;
import com.example.olx.presentation.gui.MainGuiApp;
import com.example.olx.presentation.gui.util.GlobalContext;
import com.example.olx.presentation.gui.mediator.AdBrowserMediator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.Contract;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.example.olx.presentation.gui.MainGuiApp.adService;
import static com.example.olx.presentation.gui.MainGuiApp.categoryService;

public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    @FXML private BorderPane mainBorderPane; // [cite: 6]
    @FXML private TextField searchField; // [cite: 7]
    @FXML private Button searchButton; // [cite: 7]
    @FXML private Button createAdButton; // [cite: 7]
    @FXML private Label loggedInUserLabel; // [cite: 7]
    @FXML private Button logoutButton; // [cite: 7]
    @FXML private Button exitButton; // [cite: 8]
    @FXML private TreeView<CategoryComponent> categoryTreeView; // [cite: 8]
    @FXML private Label currentCategoryLabel; // [cite: 8]
    @FXML private ListView<AdComponent> adListView; // [cite: 8]
    @FXML private HBox paginationControls; // [cite: 8]
    // Розширений пошук
    @FXML private Button advancedSearchButton; // [cite: 9]
    @FXML private HBox advancedSearchPanel; // [cite: 9]
    @FXML private TextField minPriceField; // [cite: 9]
    @FXML private TextField maxPriceField; // [cite: 10]
    @FXML private ComboBox<String> statusFilterCombo; // [cite: 10]
    @FXML private CheckBox premiumOnlyCheckBox; // [cite: 10]
    @FXML private CheckBox urgentOnlyCheckBox; // [cite: 10]
    @FXML private Button applyFiltersButton; // [cite: 10]
    @FXML private Button clearFiltersButton; // [cite: 11]

    // Сортування та відображення
    @FXML private ComboBox<String> sortComboBox; // [cite: 11]
    @FXML private Button sortOrderButton; // [cite: 11]
    // @FXML private Button listViewButton; // Removed
    // @FXML private Button gridViewButton; // Removed
    @FXML private Button refreshButton; // [cite: 13]
    // Активні фільтри
    @FXML private HBox activeFiltersPanel; // [cite: 13]
    @FXML private ScrollPane activeFiltersScrollPane; // [cite: 14]
    @FXML private HBox activeFiltersContainer; // [cite: 14]
    @FXML private Button clearAllFiltersButton; // [cite: 14]
    // Пагінація
    @FXML private Button firstPageButton; // [cite: 15]
    @FXML private Button prevPageButton; // [cite: 15]
    @FXML private Label pageInfoLabel; // [cite: 15]
    @FXML private Button nextPageButton; // [cite: 16]
    @FXML private Button lastPageButton; // [cite: 16]
    @FXML private ComboBox<Integer> pageSizeComboBox; // [cite: 16]
    // Статистика
    @FXML private Label totalAdsLabel; // [cite: 17]
    @FXML private Label filteredAdsLabel; // [cite: 17]
    @FXML private Label selectedCategoryLabel; // [cite: 17]
    // Статус бар
    @FXML private Label statusLabel; // [cite: 18]
    @FXML private Label lastUpdateLabel; // [cite: 18]
    @FXML private Label mediatorStatusLabel; // [cite: 18]
    @FXML private HBox loadingIndicator; // [cite: 19]
    @FXML private Label loadingLabel; // [cite: 19]

    // private AdCommandManager commandManager; // Removed
    private final ObservableList<AdComponent> adsObservableList = FXCollections.observableArrayList(); // [cite: 20]
    // private final ObservableList<String> commandHistoryObservableList = FXCollections.observableArrayList(); // Removed
    private String currentSelectedCategoryId = null; // [cite: 21]
    // Додаємо компоненти медіатора
    private AdBrowserMediator mediator; // [cite: 21]
    private SearchComponent searchComponent; // [cite: 22]
    private AdListComponent adListComponent; // [cite: 22]
    private FilterComponent filterComponent; // [cite: 22]
    // Додаткові змінні для пагінації та сортування
    private int currentPage = 1; // [cite: 22]
    private int pageSize = 20; // Default page size // [cite: 23]
    private boolean isAscendingSort = true; // [cite: 23]
    private String currentSortBy = "title"; // Default sort // [cite: 24]
    private boolean isAdvancedSearchVisible = false; // [cite: 24]

    @FXML
    public void initialize() {
        User currentUser = GlobalContext.getInstance().getLoggedInUser(); // [cite: 25]
        if (currentUser != null) { // [cite: 26]
            loggedInUserLabel.setText("Користувач: " + currentUser.getUsername()); // [cite: 26]
            if (createAdButton != null) createAdButton.setDisable(false); // [cite: 27]
            if (logoutButton != null) logoutButton.setDisable(false); // [cite: 27]
        } else { // [cite: 28]
            try {
                MainGuiApp.loadLoginScene(); // [cite: 28]
                return; // Stop further initialization if not logged in // [cite: 29]
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to load login scene", e); // [cite: 29]
                showErrorAlert("Помилка входу", "Не вдалося завантажити сторінку входу.", e.getMessage()); // [cite: 30]
                Platform.exit(); // Exit if login scene fails to load // [cite: 30]
                return; // [cite: 30]
            }
        }

        // initializeCommandManager(); // Removed
        initializeMediator(); // [cite: 32]
        initializeUIComponents(); // [cite: 32]
        setupCategoryTree(); // [cite: 32]
        setupAdListView(); // [cite: 32]
        // setupCommandHistoryView(); // Removed
        setupMediatorIntegration(); // [cite: 33]
        setupGlobalEventListeners(); // [cite: 33]
        // Initial data load via mediator and UI updates // [cite: 34]
        if (this.mediator != null) { // [cite: 34]
            this.mediator.loadAllAds(); // [cite: 34]
        } else { // [cite: 35]
            loadAds(null); // [cite: 35]
        }

        // updateCommandButtons(); // Removed
        updateLastUpdateTime(); // [cite: 36]
        updateStatus("Головне вікно ініціалізовано."); // [cite: 37]
    }

    private void initializeUIComponents() {
        // Initialize ComboBoxes
        ObservableList<String> sortOptions = FXCollections.observableArrayList( // [cite: 37]
                "За назвою", "За ціною", "За датою", "За популярністю" // Match with handleSortChange cases // [cite: 37]
        );
        if (sortComboBox != null) { // [cite: 38]
            sortComboBox.setItems(sortOptions); // [cite: 38]
            sortComboBox.setValue("За назвою"); // [cite: 38]
            sortComboBox.setOnAction(e -> handleSortChange()); // [cite: 39]
        }

        ObservableList<String> statusOptions = FXCollections.observableArrayList( // [cite: 39]
                "Всі", "Активне", "Чернетка", "Архівоване", "Продано" // [cite: 39]
        );
        if (statusFilterCombo != null) { // [cite: 40]
            statusFilterCombo.setItems(statusOptions); // [cite: 40]
            statusFilterCombo.setValue("Всі"); // [cite: 40]
        }

        ObservableList<Integer> pageSizeOptions = FXCollections.observableArrayList( // [cite: 41]
                10, 20, 50, 100 // [cite: 41]
        );
        if (pageSizeComboBox != null) { // [cite: 42]
            pageSizeComboBox.setItems(pageSizeOptions); // [cite: 42]
            pageSizeComboBox.setValue(pageSize); // [cite: 42]
            pageSizeComboBox.setOnAction(e -> handlePageSizeChange()); // [cite: 43]
        }

        // setupQuickFilters(); // Removed
        if (advancedSearchPanel != null) { // [cite: 44]
            advancedSearchPanel.setVisible(false); // [cite: 44]
            advancedSearchPanel.setManaged(false); // [cite: 45]
        }
    }

    private void setupAdListView() {
        if (adListView == null) { // [cite: 45]
            LOGGER.severe("Error: adListView is null. Check FXML binding."); // [cite: 45]
            return; // [cite: 46]
        }
        adListView.setCellFactory(listView -> new ListCell<AdComponent>() { // [cite: 46]
            @Override
            protected void updateItem(AdComponent adComponent, boolean empty) {
                super.updateItem(adComponent, empty);

                if (empty || adComponent == null || adComponent.getAd() == null) { // [cite: 46]
                    setText(null); // [cite: 47]
                    setGraphic(null); // [cite: 47]
                } else {
                    Ad ad = adComponent.getAd(); // [cite: 47]
                    VBox container = new VBox(5); // [cite: 47]
                    container.setPadding(new Insets(10)); // [cite: 48]

                    Label titleLabel = new Label(ad.getTitle()); // [cite: 48]
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;"); // [cite: 48]

                    Label priceLabel = new Label(String.format("%.2f грн", ad.getPrice())); // [cite: 48]
                    priceLabel.setStyle("-fx-text-fill: #2E8B57; -fx-font-weight: bold;"); // [cite: 49]

                    String description = ad.getDescription(); // [cite: 49]
                    if (description != null && description.length() > 100) { // [cite: 49]
                        description = description.substring(0, 100) + "..."; // [cite: 49]
                    }
                    Label descLabel = new Label(description != null ? description : "Немає опису"); // [cite: 50]
                    descLabel.setStyle("-fx-text-fill: #666666;"); // [cite: 51]

                    HBox infoBox = new HBox(15); // [cite: 51]
                    String categoryName = "Невідомо"; // [cite: 51]
                    if (ad.getCategoryId() != null && categoryService != null) { // [cite: 52]
                        Optional<Category> categoryOptional = categoryService.getCategoryById(ad.getCategoryId()); // [cite: 52]
                        categoryName = categoryOptional // [cite: 53]
                                .map(Category::getName) // [cite: 53]
                                .orElse("ID: " + ad.getCategoryId()); // [cite: 53]
                    } else if (ad.getCategoryId() != null) { // [cite: 54]
                        categoryName = "ID: " + ad.getCategoryId(); // [cite: 54]
                    }
                    Label categoryInfoLabel = new Label("Категорія: " + categoryName); // [cite: 55]
                    String dateStr = "Дата: невідома"; // [cite: 56]
                    if (ad.getCreatedAt() != null) { // [cite: 56]
                        try {
                            dateStr = "Дата: " + DateUtils.formatDate(ad.getCreatedAt()); // [cite: 56]
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error formatting date for ad: " + ad.getId(), e); // [cite: 57]
                            // Consistent fallback format // [cite: 58]
                            dateStr = "Дата: " + (ad.getCreatedAt() != null ? // [cite: 58]
                                    ad.getCreatedAt().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : // [cite: 58]
                                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))) + // [cite: 59]
                                    " (fallback format error)"; // [cite: 59]
                        }
                    }
                    Label dateLabel = new Label(dateStr); // [cite: 60]
                    infoBox.getChildren().addAll(categoryInfoLabel, dateLabel); // [cite: 61]

                    container.getChildren().addAll(titleLabel, priceLabel, descLabel, infoBox);

                    // Display decorator info only if it exists
                    String decoratorInfo = adComponent.getDisplayInfo();
                    if (decoratorInfo != null && !decoratorInfo.trim().isEmpty()) {
                        Label decoratedInfoLabel = new Label(decoratorInfo);
                        decoratedInfoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: blue;"); // [cite: 61]
                        container.getChildren().add(decoratedInfoLabel);
                    }
                    setGraphic(container); // [cite: 61]
                }
            }
        });
        adListView.setOnMouseClicked(event -> { // [cite: 63]
            if (event.getClickCount() == 2) { // [cite: 63]
                AdComponent selectedComponent = adListView.getSelectionModel().getSelectedItem(); // [cite: 63]
                if (selectedComponent != null && selectedComponent.getAd() != null) { // [cite: 63]
                    try {
                        MainGuiApp.loadAdDetailScene(selectedComponent.getAd()); // [cite: 64]
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error opening ad details", e); // [cite: 64]
                        showError("Помилка відкриття деталей оголошення: " + e.getMessage()); // [cite: 64]
                    }
                }
            }
        });
    }

    // Fix 4: DateUtils Usage
    public static class DateUtils { // Зроблено статичним вкладеним класом для кращої організації // [cite: 66]

        public static LocalDate toLocalDate(LocalDateTime dateTime) {
            return dateTime != null ? // [cite: 66]
                    dateTime.toLocalDate() : null; // [cite: 67]
        }

        public static String formatDate(LocalDateTime dateTime) {
            return dateTime != null ? // [cite: 67]
                    dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : ""; // [cite: 68]
        }

        public static String formatDate(LocalDate date) {
            return date != null ? // [cite: 68]
                    date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : ""; // [cite: 69]
        }
    }

    private void setupGlobalEventListeners() {
        if (categoryTreeView != null) { // [cite: 69]
            categoryTreeView.getSelectionModel().selectedItemProperty().addListener( // [cite: 69]
                    (observable, oldValue, newValueNode) -> {
                        if (newValueNode != null) { // [cite: 69]
                            CategoryComponent selectedCategory = newValueNode.getValue(); // [cite: 70]
                            if (selectedCategory != null && !"root".equals(selectedCategory.getId()) && selectedCategory.getName() != null) { // [cite: 70]
                                currentSelectedCategoryId = selectedCategory.getId(); // [cite: 70]
                                String categoryName = selectedCategory.getName(); // [cite: 71]
                                if (currentCategoryLabel != null) currentCategoryLabel.setText("Оголошення в категорії: " + categoryName); // [cite: 71]
                                if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: " + categoryName); // [cite: 72]
                                if (searchComponent != null) { // [cite: 72]
                                    searchComponent.updateCategory(currentSelectedCategoryId); // [cite: 72]
                                    searchComponent.performSearch(searchField != null ? searchField.getText() : "", currentSelectedCategoryId); // [cite: 73]
                                } else {
                                    loadAds(currentSelectedCategoryId); // [cite: 73]
                                }
                            } else if (selectedCategory != null && "root".equals(selectedCategory.getId())) { // [cite: 74]
                                currentSelectedCategoryId = null; // [cite: 74]
                                if (currentCategoryLabel != null) currentCategoryLabel.setText("Всі оголошення"); // [cite: 75]
                                if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: Всі"); // [cite: 75]
                                if (searchComponent != null) { // [cite: 76]
                                    searchComponent.updateCategory(null); // [cite: 76]
                                    searchComponent.performSearch(searchField != null ? searchField.getText() : "", null); // [cite: 77]
                                } else {
                                    loadAds(null); // [cite: 77]
                                }
                            }
                        } else {
                            currentSelectedCategoryId = null; // [cite: 78]
                            if (currentCategoryLabel != null) currentCategoryLabel.setText("Всі оголошення"); // [cite: 79]
                            if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: немає"); // [cite: 79]
                            if (searchComponent != null) { // [cite: 80]
                                searchComponent.updateCategory(null); // [cite: 80]
                                searchComponent.performSearch(searchField != null ? searchField.getText() : "", null); // [cite: 81]
                            } else {
                                loadAds(null); // [cite: 81]
                            }
                        }
                    });
        }
    }

    // private void setupQuickFilters() { // Removed
    // }

    // private void applyQuickFilters() { // Removed
    // }

    private void initializeMediator() {
        if (adService == null || categoryService == null) { // [cite: 83]
            showErrorAlert("Критична помилка", "Сервіси не ініціалізовані.", "Неможливо створити медіатор."); // [cite: 83]
            updateMediatorStatus("помилка ініціалізації"); // [cite: 84]
            return; // [cite: 84]
        }
        mediator = new AdBrowserMediator(adService, categoryService); // [cite: 84]
        searchComponent = new SearchComponent(mediator); // [cite: 84]
        adListComponent = new AdListComponent(mediator); // [cite: 85]
        filterComponent = new FilterComponent(mediator); // [cite: 85]
        mediator.registerComponents(searchComponent, adListComponent, filterComponent); // [cite: 85]
        mediator.setController(this); // [cite: 85]

        LOGGER.info("Медіатор ініціалізовано успішно."); // [cite: 85]
        updateMediatorStatus("активний"); // [cite: 85]
    }

    private void setupMediatorIntegration() {
        if (searchField == null || searchButton == null) { // [cite: 86]
            LOGGER.warning("Search UI components (field or button) not initialized for mediator integration."); // [cite: 86]
            if (searchButton != null) searchButton.setOnAction(e -> handleSearchAds()); // [cite: 87]
            if (searchField != null) searchField.setOnAction(e -> handleSearchAds()); // [cite: 87]
            return; // [cite: 87]
        }

        searchButton.setOnAction(e -> { // [cite: 88]
            String searchText = searchField.getText(); // [cite: 88]
            if (searchComponent != null) { // [cite: 88]
                searchComponent.performSearch(searchText, currentSelectedCategoryId); // Mediator handles loading // [cite: 88]
                updateStatus("Пошук ініційовано через медіатор: " + searchText); // [cite: 88]
            } else {
                LOGGER.warning("SearchComponent is null, falling back to handleSearchAds()"); // [cite: 89]
                handleSearchAds(); // [cite: 89]
            }
        });
        searchField.setOnAction(e -> { // [cite: 90]
            String searchText = searchField.getText(); // [cite: 90]
            if (searchComponent != null) { // [cite: 90]
                searchComponent.performSearch(searchText, currentSelectedCategoryId); // Mediator handles loading // [cite: 90]
                updateStatus("Пошук ініційовано через медіатор (Enter): " + searchText); // [cite: 90]
            } else {
                LOGGER.warning("SearchComponent is null, falling back to handleSearchAds()"); // [cite: 91]
                handleSearchAds(); // [cite: 91]
            }
        });
    }

    @FXML
    private void handleToggleAdvancedSearch() {
        isAdvancedSearchVisible = !isAdvancedSearchVisible; // [cite: 92]
        if (advancedSearchPanel != null) { // [cite: 93]
            advancedSearchPanel.setVisible(isAdvancedSearchVisible); // [cite: 93]
            advancedSearchPanel.setManaged(isAdvancedSearchVisible); // [cite: 93]
        }
        updateStatus("Розширений пошук " + (isAdvancedSearchVisible ? "відкрито" : "закрито")); // [cite: 94]
    }

    @FXML
    private void handleApplyFilters() {
        String minPriceText = (minPriceField != null) ? // [cite: 95]
                minPriceField.getText() : ""; // [cite: 96]
        String maxPriceText = (maxPriceField != null) ? maxPriceField.getText() : ""; // [cite: 96]
        String selectedStatus = (statusFilterCombo != null && statusFilterCombo.getValue() != null) ? statusFilterCombo.getValue() : "Всі"; // [cite: 97]
        boolean premiumOnlyAdv = premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected(); // [cite: 98]
        boolean urgentOnlyAdv = urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected(); // [cite: 98]

        Double minPrice = null; // [cite: 98]
        Double maxPrice = null; // [cite: 99]
        try {
            if (minPriceText != null && !minPriceText.isEmpty()) { // [cite: 99]
                minPrice = Double.parseDouble(minPriceText); // [cite: 99]
            }
            if (maxPriceText != null && !maxPriceText.isEmpty()) { // [cite: 100]
                maxPrice = Double.parseDouble(maxPriceText); // [cite: 100]
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Помилка фільтрації", "Невірний формат ціни", "Будь ласка, введіть коректні числові значення для ціни."); // [cite: 101]
            return; // [cite: 102]
        }

        String keyword = (searchField != null) ? searchField.getText() : ""; // [cite: 102]
        loadAdsWithAdvancedFilters(keyword, minPrice, maxPrice, currentSelectedCategoryId, selectedStatus, premiumOnlyAdv, urgentOnlyAdv); // [cite: 103]
    }

    private void loadAdsWithAdvancedFilters(String keyword, Double minPrice, Double maxPrice, String categoryId, String status, boolean premiumOnlyAdv, boolean urgentOnlyAdv) {
        if (adService == null) { // [cite: 103]
            showErrorAlert("Помилка", "Сервіс оголошень недоступний.", "Неможливо завантажити оголошення."); // [cite: 103]
            return; // [cite: 104]
        }
        showLoadingIndicator("Завантаження оголошень з фільтрами..."); // [cite: 104]
        Task<List<AdComponent>> loadTask = new Task<>() { // [cite: 105]
            @Override
            protected List<AdComponent> call() throws Exception {
                List<Ad> fetchedAds = adService.searchAds(keyword, minPrice, maxPrice, categoryId); // [cite: 105]
                List<Ad> filteredAds = new ArrayList<>(); // [cite: 106]
                if (fetchedAds != null) { // [cite: 106]
                    for (Ad ad : fetchedAds) { // [cite: 106]
                        if (ad == null) continue; // [cite: 106]
                        boolean statusMatch = "Всі".equals(status) || (ad.getStatus() != null && status.equals(ad.getStatus().toString())); // [cite: 107]
                        if (!statusMatch) continue; // [cite: 107]

                        if (premiumOnlyAdv && !ad.isPremium()) continue; // [cite: 107]
                        if (urgentOnlyAdv && !ad.isUrgent()) continue; // [cite: 108]

                        // Removed quick filter checks
                        // if (quickFilterPremium != null && quickFilterPremium.isSelected() && !ad.isPremium()) continue; // [cite: 108]
                        // if (quickFilterUrgent != null && quickFilterUrgent.isSelected() && !ad.isUrgent()) continue; // [cite: 109]
                        // if (quickFilterWithDelivery != null && quickFilterWithDelivery.isSelected() && !ad.hasDelivery()) continue; // [cite: 109]
                        // if (quickFilterWithWarranty != null && quickFilterWithWarranty.isSelected() && !ad.hasWarranty()) continue; // [cite: 110]
                        // if (quickFilterWithDiscount != null && quickFilterWithDiscount.isSelected() && !ad.hasDiscount()) continue; // [cite: 110]
                        filteredAds.add(ad); // [cite: 111]
                    }
                }
                return filteredAds.stream() // [cite: 111]
                        .map(MainController.this::createDecoratedAd) // [cite: 111]
                        .filter(Objects::nonNull) // [cite: 111]
                        .collect(Collectors.toList()); // [cite: 112]
            }
        };
        loadTask.setOnSucceeded(event -> { // [cite: 113]
            List<AdComponent> decoratedAds = loadTask.getValue(); // [cite: 113]
            adsObservableList.setAll(decoratedAds); // [cite: 113]
            applySorting(); // [cite: 113]
            updatePaginationControls(); // [cite: 113]
            updateActiveFiltersDisplay(); // [cite: 113]
            updateStatistics(); // [cite: 113]
            hideLoadingIndicator(); // [cite: 113]
            updateStatus("Фільтри застосовано. Знайдено " + decoratedAds.size() + " оголошень (до пагінації)."); // [cite: 114]
        });
        loadTask.setOnFailed(event -> { // [cite: 115]
            LOGGER.log(Level.SEVERE, "Failed to load ads with advanced filters", loadTask.getException()); // [cite: 115]
            hideLoadingIndicator(); // [cite: 115]
            showErrorAlert("Помилка завантаження", "Не вдалося завантажити оголошення з фільтрами.", loadTask.getException().getMessage()); // [cite: 115]
        });
        new Thread(loadTask).start(); // [cite: 116]
    }

    @FXML
    private void handleClearFilters() {
        if (minPriceField != null) minPriceField.clear(); // [cite: 116]
        if (maxPriceField != null) maxPriceField.clear(); // [cite: 117]
        if (statusFilterCombo != null) statusFilterCombo.setValue("Всі"); // [cite: 117]
        if (premiumOnlyCheckBox != null) premiumOnlyCheckBox.setSelected(false); // [cite: 117]
        if (urgentOnlyCheckBox != null) urgentOnlyCheckBox.setSelected(false); // [cite: 117]
        refreshCurrentView(); // [cite: 118]
        updateActiveFiltersDisplay(); // [cite: 118]
        updateStatus("Фільтри розширеного пошуку очищено"); // [cite: 118]
    }

    @FXML
    private void handleClearAllFilters() {
        if (minPriceField != null) minPriceField.clear(); // [cite: 118]
        if (maxPriceField != null) maxPriceField.clear(); // [cite: 119]
        if (statusFilterCombo != null) statusFilterCombo.setValue("Всі"); // [cite: 119]
        if (premiumOnlyCheckBox != null) premiumOnlyCheckBox.setSelected(false); // [cite: 119]
        if (urgentOnlyCheckBox != null) urgentOnlyCheckBox.setSelected(false); // [cite: 119]
        // Removed quick filter reset // [cite: 120]
        // if (quickFilterPremium != null) quickFilterPremium.setSelected(false); // [cite: 120]
        // if (quickFilterUrgent != null) quickFilterUrgent.setSelected(false); // [cite: 121]
        // if (quickFilterWithDelivery != null) quickFilterWithDelivery.setSelected(false); // [cite: 121]
        // if (quickFilterWithWarranty != null) quickFilterWithWarranty.setSelected(false); // [cite: 121]
        // if (quickFilterWithDiscount != null) quickFilterWithDiscount.setSelected(false); // [cite: 122]

        refreshCurrentView(); // [cite: 122]
        updateActiveFiltersDisplay(); // [cite: 122]
        updateStatus("Всі фільтри очищено"); // [cite: 122]
    }

    @FXML
    private void handleToggleSortOrder() {
        isAscendingSort = !isAscendingSort; // [cite: 123]
        if (sortOrderButton != null) { // [cite: 124]
            sortOrderButton.setText(isAscendingSort ? "↑" : "↓"); // [cite: 124]
        }
        applySorting(); // [cite: 125]
        updatePaginationControls(); // [cite: 125]
        updateStatus("Порядок сортування змінено на " + (isAscendingSort ? "зростаючий" : "спадаючий")); // [cite: 126]
    }

    private void handleSortChange() {
        if (sortComboBox == null || sortComboBox.getValue() == null) return; // [cite: 127]
        String selectedSort = sortComboBox.getValue(); // [cite: 128]
        switch (selectedSort) { // [cite: 128]
            case "За назвою":
                currentSortBy = "title"; // [cite: 128]
                break; // [cite: 129]
            case "За ціною":
                currentSortBy = "price"; // [cite: 129]
                break; // [cite: 130]
            case "За датою":
                currentSortBy = "date"; // [cite: 130]
                break; // [cite: 131]
            case "За популярністю":
                currentSortBy = "popularity"; // [cite: 131]
                break; // [cite: 132]
            default:
                currentSortBy = "title"; // [cite: 132]
                break; // [cite: 133]
        }
        applySorting(); // [cite: 133]
        updatePaginationControls(); // [cite: 133]
        updateStatus("Сортування змінено на: " + selectedSort); // [cite: 133]
    }

    private void applySorting() {
        if (adsObservableList != null && !adsObservableList.isEmpty()) { // [cite: 134]
            adsObservableList.sort((ac1, ac2) -> { // [cite: 134]
                if (ac1 == null && ac2 == null) return 0; // [cite: 134]
                if (ac1 == null || ac1.getAd() == null) return isAscendingSort ? 1 : -1; // [cite: 134]
                if (ac2 == null || ac2.getAd() == null) return isAscendingSort ? -1 : 1; // [cite: 135]

                Ad ad1 = ac1.getAd(); // [cite: 135]
                Ad ad2 = ac2.getAd(); // [cite: 135]
                int comparisonResult = 0; // [cite: 135]

                switch (currentSortBy) { // [cite: 135]
                    case "title": // [cite: 136]
                        comparisonResult = Objects.compare(ad1.getTitle(), ad2.getTitle(), String::compareToIgnoreCase); // [cite: 136]
                        break;
                    case "price":
                        comparisonResult = Double.compare(ad1.getPrice(), ad2.getPrice()); // [cite: 137]
                        break;
                    case "date":
                        comparisonResult = Objects.compare(ad1.getCreatedAt(), ad2.getCreatedAt(), LocalDateTime::compareTo); // [cite: 137]
                        break; // [cite: 138]
                    case "popularity":
                        // Placeholder: Implement actual popularity logic if Ad model supports it. // [cite: 138]
                        // For now, let's sort by creation date descending as a proxy for popularity (newer first). // [cite: 139]
                        // Ensure Ad has a getCreatedAt() method returning LocalDateTime. // [cite: 140]
                        if (ad1.getCreatedAt() != null && ad2.getCreatedAt() != null) { // [cite: 140]
                            comparisonResult = ad2.getCreatedAt().compareTo(ad1.getCreatedAt()); // [cite: 140]
                        } else { // [cite: 141]
                            comparisonResult = Objects.compare(ad1.getCreatedAt(), ad2.getCreatedAt(), Comparator.nullsLast(LocalDateTime::compareTo)); // [cite: 141]
                        }
                        break; // [cite: 142]
                    default: // [cite: 143]
                        break; // [cite: 143]
                }
                return isAscendingSort ? // [cite: 144]
                        comparisonResult : -comparisonResult; // [cite: 145]
            });
        }
        LOGGER.info("Applying sort to full list by: " + currentSortBy + (isAscendingSort ? " ASC" : " DESC")); // [cite: 145]
    }

    // @FXML // Removed
    // private void handleSwitchToListView() {
    // if (listViewButton != null) listViewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // [cite: 146]
    // if (gridViewButton != null) gridViewButton.setStyle(""); // [cite: 147]
    // updateStatus("Перемкнуто на вигляд списку"); // [cite: 147]
    // }

    // @FXML // Removed
    // private void handleSwitchToGridView() {
    // if (gridViewButton != null) gridViewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // [cite: 148]
    // if (listViewButton != null) listViewButton.setStyle(""); // [cite: 149]
    // updateStatus("Перемкнуто на вигляд сітки"); // [cite: 149]
    // }

    @FXML
    private void handleRefresh() {
        refreshCurrentView(); // [cite: 150]
        updateLastUpdateTime(); // [cite: 151]
        updateStatus("Список оновлено"); // [cite: 151]
    }

    @FXML
    private void handleFirstPage() {
        if (currentPage > 1) { // [cite: 151]
            currentPage = 1; // [cite: 151]
            updatePaginationControls(); // [cite: 152]
            updateStatus("Перехід на першу сторінку"); // [cite: 152]
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) { // [cite: 152]
            currentPage--; // [cite: 152]
            updatePaginationControls(); // [cite: 153]
            updateStatus("Перехід на попередню сторінку"); // [cite: 153]
        }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = getTotalPages(); // [cite: 153]
        if (currentPage < totalPages) { // [cite: 154]
            currentPage++; // [cite: 154]
            updatePaginationControls(); // [cite: 154]
            updateStatus("Перехід на наступну сторінку"); // [cite: 155]
        }
    }

    @FXML
    private void handleLastPage() {
        int totalPages = getTotalPages(); // [cite: 155]
        if (currentPage < totalPages) { // [cite: 156]
            currentPage = totalPages; // [cite: 156]
            updatePaginationControls(); // [cite: 156]
            updateStatus("Перехід на останню сторінку"); // [cite: 157]
        }
    }

    private void handlePageSizeChange() {
        if (pageSizeComboBox == null || pageSizeComboBox.getValue() == null) return; // [cite: 157]
        Integer newPageSize = pageSizeComboBox.getValue(); // [cite: 158]
        if (newPageSize != null && newPageSize > 0 && newPageSize != pageSize) { // [cite: 158]
            pageSize = newPageSize; // [cite: 158]
            currentPage = 1; // [cite: 159]
            updatePaginationControls(); // [cite: 159]
            updateStatus("Розмір сторінки змінено на " + pageSize); // [cite: 159]
        }
    }

    private void updatePaginationControls() {
        if (adsObservableList == null) { // [cite: 160]
            if (adListView != null) adListView.setItems(FXCollections.emptyObservableList()); // [cite: 160]
            if (pageInfoLabel != null) pageInfoLabel.setText("Сторінка 0 з 0"); // [cite: 161]
            if (firstPageButton != null) firstPageButton.setDisable(true); // [cite: 161]
            if (prevPageButton != null) prevPageButton.setDisable(true); // [cite: 161]
            if (nextPageButton != null) nextPageButton.setDisable(true); // [cite: 162]
            if (lastPageButton != null) lastPageButton.setDisable(true); // [cite: 162]
            if (paginationControls != null) { // [cite: 163]
                paginationControls.setVisible(false); // [cite: 163]
                paginationControls.setManaged(false); // [cite: 164]
            }
            updateStatistics(); // [cite: 164]
            return; // [cite: 164]
        }

        int totalItems = adsObservableList.size(); // [cite: 165]
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize)); // totalPages має бути доступний тут // [cite: 166]
        currentPage = Math.max(1, Math.min(currentPage, totalPages)); // [cite: 166]
        if (pageInfoLabel != null) { // [cite: 167]
            pageInfoLabel.setText("Сторінка " + currentPage + " з " + totalPages); // [cite: 167]
        }
        if (firstPageButton != null) firstPageButton.setDisable(currentPage <= 1); // [cite: 168]
        if (prevPageButton != null) prevPageButton.setDisable(currentPage <= 1); // [cite: 169]
        if (nextPageButton != null) nextPageButton.setDisable(currentPage >= totalPages); // [cite: 169]
        if (lastPageButton != null) lastPageButton.setDisable(currentPage >= totalPages); // [cite: 170]

        boolean paginationVisible = totalPages > 1; // [cite: 170]
        if (paginationControls != null) { // [cite: 171]
            paginationControls.setVisible(paginationVisible); // [cite: 171]
            paginationControls.setManaged(paginationVisible); // [cite: 171]
        }

        if (adListView != null) { // [cite: 172]
            if (totalItems == 0) { // [cite: 172]
                adListView.setItems(FXCollections.emptyObservableList()); // [cite: 172]
            } else { // [cite: 173]
                int fromIndex = (currentPage - 1) * pageSize; // [cite: 173]
                int toIndex = Math.min(fromIndex + pageSize, totalItems); // [cite: 174]

                if (fromIndex >= 0 && fromIndex < totalItems && toIndex <= totalItems && fromIndex <= toIndex) { // [cite: 174]
                    List<AdComponent> pageData = adsObservableList.subList(fromIndex, toIndex); // [cite: 174]
                    adListView.setItems(FXCollections.observableArrayList(pageData)); // [cite: 175]
                } else if (fromIndex >= totalItems && totalItems > 0) { // [cite: 175]
                    currentPage = totalPages; // [cite: 175]
                    int adjustedFromIndex = (currentPage - 1) * pageSize; // [cite: 176]
                    int adjustedToIndex = Math.min(adjustedFromIndex + pageSize, totalItems); // [cite: 176]
                    if (adjustedFromIndex < adjustedToIndex) { // [cite: 177]
                        List<AdComponent> pageData = adsObservableList.subList(adjustedFromIndex, adjustedToIndex); // [cite: 177]
                        adListView.setItems(FXCollections.observableArrayList(pageData)); // [cite: 178]
                    } else {
                        adListView.setItems(FXCollections.emptyObservableList()); // [cite: 178]
                    }
                } else if (fromIndex >= toIndex) { // [cite: 179]
                    adListView.setItems(FXCollections.emptyObservableList()); // [cite: 179]
                } else { // [cite: 180]
                    LOGGER.severe("Pagination error (unexpected state): fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", totalItems=" + totalItems + ", currentPage=" + currentPage); // [cite: 180]
                    adListView.setItems(FXCollections.emptyObservableList()); // [cite: 181]
                }
            }
        }
        updateStatistics(); // [cite: 181]
    }


    private int getTotalPages() {
        if (adsObservableList == null || adsObservableList.isEmpty() || pageSize <= 0) return 1; // [cite: 182]
        int totalAds = adsObservableList.size(); // [cite: 183]
        return Math.max(1, (int) Math.ceil((double) totalAds / pageSize)); // [cite: 183]
    }

    private void updateActiveFiltersDisplay() {
        if (activeFiltersContainer == null || activeFiltersPanel == null) return; // [cite: 184]
        activeFiltersContainer.getChildren().clear(); // [cite: 185]
        boolean hasActiveFilters = false; // [cite: 185]

        String minPriceText = (minPriceField != null) ? minPriceField.getText() : ""; // [cite: 185]
        if (minPriceText != null && !minPriceText.isEmpty()) { // [cite: 186]
            addFilterChip("Мін. ціна: " + minPriceText); // [cite: 186]
            hasActiveFilters = true; // [cite: 187]
        }
        String maxPriceText = (maxPriceField != null) ? // [cite: 187]
                maxPriceField.getText() : ""; // [cite: 188]
        if (maxPriceText != null && !maxPriceText.isEmpty()) { // [cite: 188]
            addFilterChip("Макс. ціна: " + maxPriceText); // [cite: 188]
            hasActiveFilters = true; // [cite: 189]
        }
        if (statusFilterCombo != null && statusFilterCombo.getValue() != null && !"Всі".equals(statusFilterCombo.getValue())) { // [cite: 189]
            addFilterChip("Статус: " + statusFilterCombo.getValue()); // [cite: 189]
            hasActiveFilters = true; // [cite: 190]
        }
        if (premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected()) { // [cite: 190]
            addFilterChip("Тільки преміум (розш.)"); // [cite: 190]
            hasActiveFilters = true; // [cite: 191]
        }
        if (urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected()) { // [cite: 191]
            addFilterChip("Тільки терміново (розш.)"); // [cite: 191]
            hasActiveFilters = true; // [cite: 192]
        }
        // Removed quick filter display // [cite: 192]
        // if (quickFilterPremium != null && quickFilterPremium.isSelected()) {
        // addFilterChip("⭐ Преміум"); // [cite: 192]
        // hasActiveFilters = true; // [cite: 193]
        // }
        // ... and for other quick filters

        activeFiltersPanel.setVisible(hasActiveFilters); // [cite: 193]
        activeFiltersPanel.setManaged(hasActiveFilters); // [cite: 194]
    }

    private void addFilterChip(String text) {
        if (activeFiltersContainer != null) { // [cite: 194]
            Label filterChip = new Label(text); // [cite: 194]
            filterChip.getStyleClass().add("filter-chip"); // [cite: 195]
            HBox.setMargin(filterChip, new Insets(0, 5, 0, 0)); // [cite: 195]
            activeFiltersContainer.getChildren().add(filterChip); // [cite: 195]
        }
    }

    private void updateStatistics() {
        if (totalAdsLabel != null) { // [cite: 195]
            totalAdsLabel.setText("Всього (фільтр.): " + (adsObservableList != null ? adsObservableList.size() : 0)); // [cite: 195]
        }
        if (filteredAdsLabel != null && adListView != null && adListView.getItems() != null) { // [cite: 196]
            filteredAdsLabel.setText("На сторінці: " + adListView.getItems().size()); // [cite: 196]
        }
        if (selectedCategoryLabel != null) { // [cite: 197]
            if (currentSelectedCategoryId == null) { // [cite: 197]
                selectedCategoryLabel.setText("Обрана категорія: Всі"); // [cite: 197]
            } else if (categoryTreeView != null && categoryTreeView.getSelectionModel().getSelectedItem() != null) { // [cite: 198]
                CategoryComponent selectedComp = categoryTreeView.getSelectionModel().getSelectedItem().getValue(); // [cite: 198]
                if (selectedComp != null) { // [cite: 199]
                    selectedCategoryLabel.setText("Обрана категорія: " + selectedComp.getName()); // [cite: 199]
                }
            } else if (currentCategoryLabel != null && !currentCategoryLabel.getText().equals("Всі оголошення")) { // [cite: 200]
                selectedCategoryLabel.setText(currentCategoryLabel.getText().replace("Оголошення в категорії: ", "Обрана категорія: ")); // [cite: 200]
            }
        }
    }

    private void updateMediatorStatus(String status) {
        if (mediatorStatusLabel != null) { // [cite: 201]
            mediatorStatusLabel.setText("Медіатор: " + status); // [cite: 201]
        }
    }

    private void updateLastUpdateTime() {
        if (lastUpdateLabel != null) { // [cite: 202]
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")); // [cite: 202]
            lastUpdateLabel.setText("Останнє оновлення: " + currentTime); // [cite: 203]
        }
    }

    private void updateStatus(String message) {
        if (statusLabel != null) { // [cite: 203]
            statusLabel.setText(message); // [cite: 203]
        }
    }

    private void showLoadingIndicator(String message) {
        Platform.runLater(() -> { // [cite: 204]
            if (loadingIndicator != null) { // [cite: 204]
                loadingIndicator.setVisible(true); // [cite: 204]
                loadingIndicator.setManaged(true); // [cite: 204]
            }
            if (loadingLabel != null) { // [cite: 204]
                loadingLabel.setText(message != null ? message : "Завантаження..."); // [cite: 205]
            }
        });
    }

    private void hideLoadingIndicator() {
        Platform.runLater(() -> { // [cite: 206]
            if (loadingIndicator != null) { // [cite: 206]
                loadingIndicator.setVisible(false); // [cite: 206]
                loadingIndicator.setManaged(false); // [cite: 206]
            }
            if (loadingLabel != null) { // [cite: 206]
                loadingLabel.setText(""); // [cite: 207]
            }
        });
    }

    // private void initializeCommandManager() { // Removed
    // }

    // private void setupCommandHistoryView() { // Removed
    // }

    // private void updateCommandButtons() { // Removed
    // }

    private void setupCategoryTree() {
        if (categoryTreeView == null || categoryService == null) { // [cite: 208]
            LOGGER.severe("Error: categoryTreeView or categoryService is null. Cannot setup category tree."); // [cite: 208]
            if (categoryTreeView != null) { // [cite: 209]
                TreeItem<CategoryComponent> fallbackRoot = new TreeItem<>(new Category("fallback", "Помилка завантаження категорій", null)); // [cite: 209]
                categoryTreeView.setRoot(fallbackRoot); // [cite: 210]
            }
            return; // [cite: 210]
        }

        try {
            List<CategoryComponent> rootCategories = categoryService.getAllRootCategories(); // [cite: 211]
            if (rootCategories == null) { // [cite: 212]
                rootCategories = new ArrayList<>(); // [cite: 212]
                LOGGER.warning("Warning: CategoryService.getAllRootCategories() returned null. Using empty list."); // [cite: 213]
            }

            Category allCategoriesDataNode = new Category("root", "Всі категорії", null); // [cite: 213]
            TreeItem<CategoryComponent> rootTreeItem = new TreeItem<>(allCategoriesDataNode); // [cite: 214]
            rootTreeItem.setExpanded(true); // [cite: 214]

            for (CategoryComponent rootCategory : rootCategories) { // [cite: 214]
                if (rootCategory != null) { // [cite: 214]
                    TreeItem<CategoryComponent> categoryItem = createTreeItem(rootCategory, true); // [cite: 214]
                    if (categoryItem != null) { // [cite: 215]
                        rootTreeItem.getChildren().add(categoryItem); // [cite: 215]
                    }
                } else { // [cite: 216]
                    LOGGER.warning("Warning: Found null root category, skipping..."); // [cite: 216]
                }
            }

            categoryTreeView.setRoot(rootTreeItem); // [cite: 217]
            categoryTreeView.setShowRoot(true); // [cite: 218]

            categoryTreeView.setCellFactory(tv -> new TreeCell<CategoryComponent>() { // [cite: 218]
                @Override
                protected void updateItem(CategoryComponent item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { // [cite: 218]
                        setText(null); // [cite: 219]
                    } else {
                        String name = item.getName(); // [cite: 219]
                        setText(name != null ? name : "Невідома категорія"); // [cite: 219]
                    }
                }
            });
        } catch (Exception e) { // [cite: 221]
            LOGGER.log(Level.SEVERE, "Error setting up category tree", e); // [cite: 221]
            Category fallbackRootData = new Category("error_root", "Помилка завантаження", null); // [cite: 222]
            TreeItem<CategoryComponent> fallbackItem = new TreeItem<>(fallbackRootData); // [cite: 222]
            fallbackItem.setExpanded(true); // [cite: 222]
            if (categoryTreeView != null) { // [cite: 223]
                categoryTreeView.setRoot(fallbackItem); // [cite: 223]
                categoryTreeView.setShowRoot(true); // [cite: 224]
            }
        }
    }

    private TreeItem<CategoryComponent> createTreeItem(CategoryComponent categoryComponent, boolean autoExpand) {
        if (categoryComponent == null) { // [cite: 224]
            LOGGER.warning("Warning: Attempting to create TreeItem for null categoryComponent, skipping."); // [cite: 224]
            return null; // [cite: 225]
        }

        TreeItem<CategoryComponent> item = new TreeItem<>(categoryComponent); // [cite: 225]
        item.setExpanded(autoExpand); // [cite: 225]
        if (categoryComponent instanceof Category) { // [cite: 226]
            Category category = (Category) categoryComponent; // [cite: 226]
            CategoryComponent[] childrenArray = category.getChildren(); // [cite: 227]

            if (childrenArray != null) { // [cite: 227]
                for (CategoryComponent childComp : childrenArray) { // [cite: 227]
                    if (childComp != null) { // [cite: 227]
                        TreeItem<CategoryComponent> childItem = createTreeItem(childComp, false); // [cite: 227]
                        if (childItem != null) { // [cite: 228]
                            item.getChildren().add(childItem); // [cite: 228]
                        }
                    } else { // [cite: 229]
                        LOGGER.warning("Warning: Found null child category in category: " + // [cite: 229]
                                (category.getName() != null ? category.getName() : "ID: " + category.getId()) + ", skipping child."); // [cite: 229]
                    }
                }
            }
        }
        return item; // [cite: 230]
    }

    private void loadAds(String categoryId) {
        LOGGER.info("Loading ads for category: " + (categoryId == null ? "All" : categoryId)); // [cite: 231]
        if (adService == null) { // [cite: 232]
            showErrorAlert("Помилка", "Сервіс оголошень недоступний.", "Неможливо завантажити оголошення."); // [cite: 232]
            hideLoadingIndicator(); // [cite: 233]
            return; // [cite: 233]
        }
        showLoadingIndicator("Завантаження оголошень..."); // [cite: 233]
        String keyword = (searchField != null) ? // [cite: 233]
                searchField.getText() : ""; // [cite: 234]

        Task<List<AdComponent>> loadTask = new Task<>() { // [cite: 234]
            @Override
            protected List<AdComponent> call() throws Exception {
                List<Ad> ads = adService.searchAds(keyword, null, null, categoryId); // [cite: 234]
                List<Ad> filteredByQuickFilters = new ArrayList<>(); // Renamed for clarity, though quick filters are removed // [cite: 235]
                if (ads != null) { // [cite: 235]
                    for (Ad ad : ads) { // [cite: 235]
                        if (ad == null) continue; // [cite: 235]
                        // Quick filter logic removed // [cite: 236]
                        filteredByQuickFilters.add(ad); // [cite: 236]
                    }
                } else { // [cite: 237]
                    ads = new ArrayList<>(); // [cite: 237]
                }

                LOGGER.info("Total ads fetched by service: " + (ads != null ? ads.size() : 0)); // [cite: 238]
                LOGGER.info("Ads after (removed) quick filtering: " + filteredByQuickFilters.size()); // This log might be misleading now // [cite: 239]

                return filteredByQuickFilters.stream() // [cite: 239]
                        .map(MainController.this::createDecoratedAd) // [cite: 239]
                        .filter(Objects::nonNull) // [cite: 239]
                        .collect(Collectors.toList()); // [cite: 240]
            }
        };
        loadTask.setOnSucceeded(event -> { // [cite: 241]
            List<AdComponent> decoratedAds = loadTask.getValue(); // [cite: 241]
            LOGGER.info("Decorated ads count for UI update: " + decoratedAds.size()); // [cite: 241]
            adsObservableList.setAll(decoratedAds); // [cite: 241]
            applySorting(); // [cite: 241]
            updatePaginationControls(); // [cite: 241]
            updateActiveFiltersDisplay(); // [cite: 241]
            updateStatistics(); // [cite: 242]
            hideLoadingIndicator(); // [cite: 242]
            updateStatus("Завантажено " + adsObservableList.size() + " відповідних оголошень (до пагінації). На сторінці: " + (adListView != null && adListView.getItems() != null ? adListView.getItems().size() : 0) ); // [cite: 242]
        });
        loadTask.setOnFailed(event -> { // [cite: 243]
            LOGGER.log(Level.SEVERE, "Failed to load ads", loadTask.getException()); // [cite: 243]
            hideLoadingIndicator(); // [cite: 243]
            showErrorAlert("Помилка завантаження", "Не вдалося завантажити оголошення.", loadTask.getException().getMessage()); // [cite: 243]
            adsObservableList.clear(); // Clear list on failure // [cite: 243]
            updatePaginationControls(); // Update UI to show empty list // [cite: 243]
            updateStatistics(); // [cite: 244]
        });
        new Thread(loadTask).start(); // [cite: 244]
    }


    private void refreshCurrentView() {
        boolean advancedFiltersActive = (minPriceField != null && !minPriceField.getText().isEmpty()) || // [cite: 245]
                (maxPriceField != null && !maxPriceField.getText().isEmpty()) || // [cite: 246]
                (statusFilterCombo != null && statusFilterCombo.getValue() != null && !"Всі".equals(statusFilterCombo.getValue())) || // [cite: 246]
                (premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected()) || // [cite: 247]
                (urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected()); // [cite: 247]
        if (advancedFiltersActive) { // [cite: 248]
            handleApplyFilters(); // [cite: 248]
        } else { // [cite: 249]
            loadAds(currentSelectedCategoryId); // [cite: 249]
        }
    }

    @FXML
    private void handleSearchAds() {
        refreshCurrentView(); // [cite: 250]
        if (searchField != null) { // [cite: 251]
            updateStatus("Пошук за запитом: " + searchField.getText()); // [cite: 251]
        }
    }

    @FXML
    public void handleCreateAd() {
        try {
            MainGuiApp.loadCreateAdScene(); // [cite: 252]
            refreshCurrentView(); // Refresh after returning from create ad scene // [cite: 253]
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open create ad form", e); // [cite: 253]
            showErrorAlert("Помилка", "Не вдалося відкрити форму створення оголошення", e.getMessage()); // [cite: 254]
        }
    }

    @FXML
    public void handleLogout() {
        try {
            MainGuiApp.loadLoginScene(); // [cite: 254]
        } catch (IOException e) { // [cite: 255]
            LOGGER.log(Level.SEVERE, "Failed to navigate to login screen", e); // [cite: 255]
            showErrorAlert("Помилка", "Не вдалося перейти до екрану входу", e.getMessage()); // [cite: 256]
        }
    }

    @FXML
    public void handleExitApplication() {
        try {
            if (MainGuiApp.sessionManager != null) { // [cite: 256]
                MainGuiApp.sessionManager.saveState(); // [cite: 256]
            }
        } catch (Exception e) { // [cite: 257]
            LOGGER.log(Level.SEVERE, "Error saving session state on exit", e); // [cite: 257]
        } finally { // [cite: 258]
            Platform.exit(); // [cite: 258]
            System.exit(0); // [cite: 258]
        }
    }

    private void showErrorAlert(String title, String message) {
        showErrorAlert(title, null, message); // [cite: 259]
    }

    private void showErrorAlert(String title, String header, String content) {
        if (title == null) { // [cite: 260]
            title = "Помилка"; // [cite: 260]
        }
        if (header == null) { // [cite: 261]
            header = "Виникла помилка"; // [cite: 261]
        }
        if (content == null || content.trim().isEmpty()) { // [cite: 262]
            content = "Невідома помилка"; // [cite: 262]
        }

        String finalContent = content; // [cite: 263]
        String finalHeader = header; // [cite: 263]
        String finalTitle = title; // [cite: 263]
        Platform.runLater(() -> { // [cite: 264]
            Alert alert = new Alert(Alert.AlertType.ERROR); // [cite: 264]
            alert.setTitle(finalTitle); // [cite: 264]
            alert.setHeaderText(finalHeader); // [cite: 264]
            alert.setContentText(finalContent); // [cite: 264]
            try {
                URL cssUrl = getClass().getResource("/styles/alert-styles.css"); // [cite: 264]
                if (cssUrl != null) { // [cite: 265]
                    alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm()); // [cite: 265]
                } else {
                    LOGGER.warning("Alert CSS /styles/alert-styles.css not found."); // [cite: 265]
                }
            } catch (Exception e) // [cite: 265]
            {
                LOGGER.log(Level.WARNING, "Failed to load alert styles", e); // [cite: 266]
            }
            alert.showAndWait(); // [cite: 266]
        });
    }


    // @FXML // Removed
    // private void handleUndo() {
    // }

    // @FXML // Removed
    // private void handleRedo() {
    // }

    // @FXML // Removed
    // private void handleClearHistory() {
    // }

    @Contract(pure = true)
    private void showError(String message) {
        if (message == null || message.trim().isEmpty()) { // [cite: 267]
            LOGGER.severe("showError called with empty message"); // [cite: 268]
            return; // [cite: 268]
        }
        LOGGER.severe("ПОМИЛКА: " + message); // [cite: 268]
    }

    @Contract(pure = true)
    private Optional<ButtonType> showConfirmationAlert(String title, String header, String content) {
        if (title == null) title = "Підтвердження"; // [cite: 269]
        if (header == null) header = "Підтвердьте дію"; // [cite: 270]
        if (content == null || content.trim().isEmpty()) { // [cite: 270]
            content = "Ви впевнені, що хочете продовжити?"; // [cite: 270]
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // [cite: 271]
        alert.setTitle(title); // [cite: 271]
        alert.setHeaderText(header); // [cite: 271]
        alert.setContentText(content); // [cite: 271]
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO); // [cite: 271]
        Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES); // [cite: 272]
        Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO); // [cite: 272]
        yesButton.setText("Так"); // [cite: 272]
        noButton.setText("Ні"); // [cite: 272]
        try {
            URL cssUrl = getClass().getResource("/styles/alert-styles.css"); // [cite: 273]
            if (cssUrl != null) { // [cite: 274]
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm()); // [cite: 274]
            } else { // [cite: 275]
                LOGGER.warning("Alert CSS /styles/alert-styles.css not found."); // [cite: 275]
            }
        } catch (Exception e) { // [cite: 276]
            LOGGER.log(Level.WARNING, "Failed to load alert styles for confirmation", e); // [cite: 276]
        }
        return alert.showAndWait(); // [cite: 277]
    }

    @Contract(pure = true)
    private void showInfoAlert(String title, String header, String content) {
        if (title == null) title = "Інформація"; // [cite: 278]
        if (header == null) header = "Повідомлення"; // [cite: 279]
        if (content == null || content.trim().isEmpty()) { // [cite: 279]
            content = "Немає додаткової інформації"; // [cite: 279]
        }

        String finalTitle = title; // [cite: 280]
        String finalHeader = header; // [cite: 280]
        String finalContent = content; // [cite: 280]
        Platform.runLater(() -> { // [cite: 281]
            Alert alert = new Alert(Alert.AlertType.INFORMATION); // [cite: 281]
            alert.setTitle(finalTitle); // [cite: 281]
            alert.setHeaderText(finalHeader); // [cite: 281]
            alert.setContentText(finalContent); // [cite: 281]

            Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK); // [cite: 281]
            okButton.setText("Гаразд"); // [cite: 281]

            try {
                URL cssUrl = getClass().getResource("/styles/alert-styles.css"); // [cite: 282]
                if (cssUrl != null) { // [cite: 282]
                    alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm()); // [cite: 282]
                } else {
                    LOGGER.warning("Alert CSS /styles/alert-styles.css not found."); // [cite: 282]
                }
            } catch (Exception e) { // [cite: 283]
                LOGGER.log(Level.WARNING, "Failed to load alert styles for info", e); // [cite: 283]
            }
            alert.showAndWait(); // [cite: 283]
        });
    }


    private AdComponent createDecoratedAd(Ad ad) {
        if (ad == null) { // [cite: 284]
            LOGGER.warning("Attempted to decorate null ad"); // [cite: 284]
            return null; // [cite: 285]
        }
        AdComponent currentComponent = AdDecoratorFactory.createBasicAd(ad); // [cite: 285]
        if (ad.hasDiscount()) { // [cite: 286]
            double discountPercentage = ad.getDiscountPercentage(); // [cite: 286]
            String discountReason = ad.getDiscountReason(); // [cite: 287]
            currentComponent = new DiscountAdDecorator(currentComponent, discountPercentage, discountReason); // [cite: 287]
        }
        if (ad.hasWarranty()) { // [cite: 288]
            int warrantyMonths = ad.getWarrantyMonths(); // [cite: 288]
            String warrantyType = ad.getWarrantyType(); // [cite: 289]
            currentComponent = new WarrantyAdDecorator(currentComponent, warrantyMonths, warrantyType); // [cite: 289]
        }
        if (ad.hasDelivery()) { // [cite: 290]
            boolean freeDelivery = ad.isFreeDelivery(); // [cite: 290]
            double deliveryCost = ad.getDeliveryCost(); // [cite: 291]
            String deliveryInfo = ad.getDeliveryInfo(); // [cite: 291]
            currentComponent = new DeliveryAdDecorator(currentComponent, freeDelivery, deliveryCost, deliveryInfo); // [cite: 291]
        }
        if (ad.isUrgent()) { // [cite: 292]
            currentComponent = new UrgentAdDecorator(currentComponent); // [cite: 292]
        }
        if (ad.isPremium()) { // [cite: 293]
            currentComponent = new PremiumAdDecorator(currentComponent); // [cite: 293]
        }
        return currentComponent; // [cite: 294]
    }

    public void updateAdsList(List<Ad> adsFromMediator) {
        LOGGER.info("Received ads from mediator: " + (adsFromMediator != null ? adsFromMediator.size() : "null")); // [cite: 295]
        if (adsFromMediator == null) { // [cite: 296]
            adsFromMediator = new ArrayList<>(); // [cite: 296]
        }

        List<AdComponent> decoratedAds = adsFromMediator.stream() // [cite: 297]
                .map(this::createDecoratedAd) // [cite: 297]
                .filter(Objects::nonNull) // [cite: 297]
                .collect(Collectors.toList()); // [cite: 297]
        LOGGER.info("Decorated ads count from mediator: " + decoratedAds.size()); // [cite: 298]

        Platform.runLater(() -> { // [cite: 298]
            LOGGER.info("Updating UI with " + decoratedAds.size() + " ads from mediator"); // [cite: 298]
            adsObservableList.setAll(decoratedAds); // [cite: 298]
            applySorting(); // [cite: 298]
            updatePaginationControls(); // [cite: 298]
            updateActiveFiltersDisplay(); // [cite: 298]
            updateStatus("Оновлено " + decoratedAds.size() + " оголошень (медіатор)"); // [cite: 298]
            hideLoadingIndicator(); // Ensure loading is hidden if mediator updates list // [cite: 299]
        });
    }

    public void updateMediatorMessage(String message) {
        Platform.runLater(() -> { // [cite: 300]
            updateStatus(message); // [cite: 300]
            updateMediatorStatus("активний (повідомлення)"); // [cite: 300]
        });
    }

    public User getCurrentUser() {
        return GlobalContext.getInstance().getLoggedInUser(); // [cite: 301]
    }

    // public void logAction(String action) { // Removed
    // }

    public void cleanup() {
        updateStatus("Очищення контролера..."); // [cite: 302]
        if (mediator != null) { // [cite: 303]
            updateMediatorStatus("неактивний (очищено)"); // [cite: 303]
        }
        LOGGER.info("MainController cleanup finished."); // [cite: 304]
    }
}
//все одно ща просив що не просив вони там для вигляду вони не працюють а треба щоб .на рахунок другого фото я теж попросив тебе що за синій текст знизу де опис декораторів або якщо їх немає то не виводь взашалі нічого знизу (виправ код по моїм зауваженням)
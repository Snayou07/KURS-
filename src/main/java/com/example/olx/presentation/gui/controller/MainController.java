package com.example.olx.presentation.gui.controller;

import com.example.olx.application.service.port.AdServicePort;
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
// import com.example.olx.presentation.gui.mediator.components.*; // Already imported individually

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
// import javafx.scene.text.Text; // Unused
// import javafx.util.Callback; // Unused for now, commented out setupAdListView uses it
// import javafx.beans.value.ChangeListener; // Used explicitly in cleanup
// import javafx.beans.value.ObservableValue; // Unused

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
// import java.util.Random; // Unused
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.example.olx.presentation.gui.MainGuiApp.adService;
import static com.example.olx.presentation.gui.MainGuiApp.categoryService;

public class MainController {

    @FXML private BorderPane mainBorderPane;
    @FXML private TextField searchField;
    @FXML private Button searchButton; // [cite: 8]
    @FXML private Button createAdButton; // [cite: 8, 31]
    @FXML private Label loggedInUserLabel; // [cite: 8]
    @FXML private Button logoutButton; // [cite: 8, 31]
    @FXML private Button exitButton; // [cite: 8]
    @FXML private TreeView<CategoryComponent> categoryTreeView; // [cite: 9]
    @FXML private Label currentCategoryLabel; // [cite: 9]
    @FXML private ListView<AdComponent> adListView; // [cite: 9]
    @FXML private HBox paginationControls; // [cite: 9]
    // Розширений пошук
    @FXML private Button advancedSearchButton; // [cite: 10]
    @FXML private HBox advancedSearchPanel; // [cite: 10]
    @FXML private TextField minPriceField; // [cite: 10]
    @FXML private TextField maxPriceField; // [cite: 11]
    @FXML private ComboBox<String> statusFilterCombo; // [cite: 11]
    @FXML private CheckBox premiumOnlyCheckBox; // [cite: 11]
    @FXML private CheckBox urgentOnlyCheckBox; // [cite: 11]
    @FXML private Button applyFiltersButton; // [cite: 11]
    @FXML private Button clearFiltersButton; // [cite: 12]

    // Швидкі фільтри
    @FXML private CheckBox quickFilterPremium; // [cite: 12]
    @FXML private CheckBox quickFilterUrgent; // [cite: 12]
    @FXML private CheckBox quickFilterWithDelivery; // [cite: 13]
    @FXML private CheckBox quickFilterWithWarranty; // [cite: 13]
    @FXML private CheckBox quickFilterWithDiscount; // [cite: 13]
    // Command pattern components
    @FXML private Button undoButton; // [cite: 14]
    @FXML private Button redoButton; // [cite: 14]
    @FXML private Button clearHistoryButton; // [cite: 14]
    @FXML private ListView<String> commandHistoryListView; // [cite: 15]

    // Сортування та відображення
    @FXML private ComboBox<String> sortComboBox; // [cite: 15]
    @FXML private Button sortOrderButton; // [cite: 15]
    @FXML private Button listViewButton; // [cite: 16]
    @FXML private Button gridViewButton; // [cite: 16]
    @FXML private Button refreshButton; // [cite: 16]
    // Активні фільтри
    @FXML private HBox activeFiltersPanel; // [cite: 17]
    @FXML private ScrollPane activeFiltersScrollPane; // [cite: 17]
    @FXML private HBox activeFiltersContainer; // [cite: 17]
    @FXML private Button clearAllFiltersButton; // [cite: 18]

    // Пагінація
    @FXML private Button firstPageButton; // [cite: 18]
    @FXML private Button prevPageButton; // [cite: 18]
    @FXML private Label pageInfoLabel; // [cite: 19]
    @FXML private Button nextPageButton; // [cite: 19]
    @FXML private Button lastPageButton; // [cite: 19]
    @FXML private ComboBox<Integer> pageSizeComboBox; // [cite: 19]
    // Статистика
    @FXML private Label totalAdsLabel; // [cite: 20]
    @FXML private Label filteredAdsLabel; // [cite: 21]
    @FXML private Label selectedCategoryLabel; // [cite: 21]
    // Статус бар
    @FXML private Label statusLabel; // [cite: 21]
    @FXML private Label lastUpdateLabel; // [cite: 22]
    @FXML private Label mediatorStatusLabel; // [cite: 22]
    @FXML private HBox loadingIndicator; // [cite: 22]
    @FXML private Label loadingLabel; // [cite: 22]

    private AdCommandManager commandManager; // [cite: 22]
    private ObservableList<AdComponent> adsObservableList = FXCollections.observableArrayList(); // [cite: 23]
    private ObservableList<String> commandHistoryObservableList = FXCollections.observableArrayList(); // [cite: 23]
    private String currentSelectedCategoryId = null; // [cite: 23]

    // Додаємо компоненти медіатора
    private AdBrowserMediator mediator; // [cite: 24]
    private SearchComponent searchComponent; // [cite: 25]
    private AdListComponent adListComponent; // Although declared, not directly used in this controller for its methods [cite: 25]
    private FilterComponent filterComponent; // [cite: 25]

    // Додаткові змінні для пагінації та сортування
    private int currentPage = 1; // [cite: 26]
    private int pageSize = 20; // Default page size [cite: 27]
    private boolean isAscendingSort = true; // [cite: 27]
    private String currentSortBy = "title"; // Default sort [cite: 28]
    private boolean isAdvancedSearchVisible = false; // [cite: 28]

    @FXML
    public void initialize() {
        User currentUser = GlobalContext.getInstance().getLoggedInUser(); // [cite: 29]
        if (currentUser != null) { // [cite: 30]
            loggedInUserLabel.setText("Користувач: " + currentUser.getUsername()); // [cite: 30]
            createAdButton.setDisable(false); // [cite: 31]
            logoutButton.setDisable(false); // [cite: 31]
        } else {
            try {
                MainGuiApp.loadLoginScene(); // [cite: 31]
                return; // Stop further initialization if not logged in [cite: 32]
            } catch (IOException e) {
                e.printStackTrace(); // [cite: 32]
                showErrorAlert("Помилка входу", "Не вдалося завантажити сторінку входу.", e.getMessage()); // [cite: 33]
                Platform.exit(); // Exit if login scene fails to load [cite: 33]
                return; // [cite: 33]
            }
        }

        initializeCommandManager(); // [cite: 34]
        initializeMediator(); // [cite: 34]
        initializeUIComponents(); // [cite: 35]
        setupCategoryTree(); // [cite: 36]
        setupAdListView(); // [cite: 37]
        setupCommandHistoryView(); // [cite: 38]
        setupMediatorIntegration(); // [cite: 39]
        setupGlobalEventListeners(); // [cite: 40]

        // Initial data load via mediator and UI updates
        if (this.mediator != null) { // [cite: 41]
            this.mediator.loadAllAds(); // [cite: 41]
        } else {
            // Fallback or direct load if mediator is not central to initial load
            loadAds(null); // [cite: 42]
        }

        updateCommandButtons(); // [cite: 43]
        updateLastUpdateTime(); // [cite: 45]
        updateStatus("Головне вікно ініціалізовано."); // [cite: 45]
    }

    private void initializeUIComponents() {
        // Initialize ComboBoxes
        ObservableList<String> sortOptions = FXCollections.observableArrayList( // [cite: 46]
                "За назвою", "За ціною", "За датою", "За популярністю" // Match with handleSortChange cases [cite: 46]
        );
        if (sortComboBox != null) { // [cite: 47]
            sortComboBox.setItems(sortOptions); // [cite: 47]
            sortComboBox.setValue("За назвою"); // [cite: 47]
            sortComboBox.setOnAction(e -> handleSortChange()); // [cite: 48]
        }

        ObservableList<String> statusOptions = FXCollections.observableArrayList( // [cite: 49]
                "Всі", "Активне", "Чернетка", "Архівоване", "Продано" // [cite: 49]
        );
        if (statusFilterCombo != null) { // [cite: 50]
            statusFilterCombo.setItems(statusOptions); // [cite: 50]
            statusFilterCombo.setValue("Всі"); // [cite: 50]
        }

        ObservableList<Integer> pageSizeOptions = FXCollections.observableArrayList( // [cite: 51]
                10, 20, 50, 100 // [cite: 51]
        );
        if (pageSizeComboBox != null) { // [cite: 52]
            pageSizeComboBox.setItems(pageSizeOptions); // [cite: 52]
            pageSizeComboBox.setValue(pageSize); // [cite: 52]
            pageSizeComboBox.setOnAction(e -> handlePageSizeChange()); // [cite: 53]
        }

        setupQuickFilters(); // [cite: 54]

        if (advancedSearchPanel != null) { // [cite: 55]
            advancedSearchPanel.setVisible(false); // [cite: 55]
            advancedSearchPanel.setManaged(false); // [cite: 56]
        }
    }

    private void setupAdListView() {
        if (adListView == null) { // [cite: 56]
            System.err.println("Error: adListView is null. Check FXML binding."); // [cite: 56]
            return; // [cite: 57]
        }
        adListView.setItems(adsObservableList); // [cite: 57]

        adListView.setCellFactory(listView -> new ListCell<AdComponent>() { // [cite: 58]
            @Override
            protected void updateItem(AdComponent adComponent, boolean empty) {
                super.updateItem(adComponent, empty);

                if (empty || adComponent == null || adComponent.getAd() == null) { // [cite: 59]
                    setText(null); // [cite: 59]
                    setGraphic(null); // [cite: 59]
                } else {
                    Ad ad = adComponent.getAd(); // [cite: 59]

                    VBox container = new VBox(5); // [cite: 60]
                    container.setPadding(new Insets(10)); // [cite: 60]

                    Label titleLabel = new Label(ad.getTitle()); // [cite: 60]
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;"); // [cite: 60]

                    Label priceLabel = new Label(String.format("%.2f грн", ad.getPrice())); // [cite: 61]
                    priceLabel.setStyle("-fx-text-fill: #2E8B57; -fx-font-weight: bold;"); // [cite: 61]

                    String description = ad.getDescription(); // [cite: 62]
                    if (description != null && description.length() > 100) { // [cite: 62]
                        description = description.substring(0, 100) + "..."; // [cite: 62]
                    }
                    Label descLabel = new Label(description != null ? description : "Немає опису"); // [cite: 63]
                    descLabel.setStyle("-fx-text-fill: #666666;"); // [cite: 63]

                    HBox infoBox = new HBox(15); // [cite: 63]
                    String categoryName = "Невідомо"; // [cite: 64]
                    if (ad.getCategoryId() != null) { // [cite: 64]
                        categoryName = "ID: " + ad.getCategoryId(); // [cite: 65]
                    }
                    Label categoryInfoLabel = new Label("Категорія: " + categoryName); // [cite: 65]
                    String dateStr = "Дата: невідома"; // [cite: 66]
                    if (ad.getCreatedAt() != null) { // [cite: 66]
                        try {
                            dateStr = "Дата: " + ad.getCreatedAt().toLocalDate().toString(); // [cite: 66]
                        } catch (Exception e) {
                            dateStr = "Дата: " + java.time.LocalDate.now().toString() + " (fallback)"; // [cite: 67]
                        }
                    }
                    Label dateLabel = new Label(dateStr); // [cite: 68]
                    infoBox.getChildren().addAll(categoryInfoLabel, dateLabel); // [cite: 69]

                    Label decoratedInfoLabel = new Label(adComponent.getDisplayInfo()); // [cite: 69]
                    decoratedInfoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: blue;"); // [cite: 70]

                    container.getChildren().addAll(titleLabel, priceLabel, descLabel, infoBox, decoratedInfoLabel); // [cite: 70]
                    setGraphic(container); // [cite: 70]
                }
            }
        });
        adListView.setOnMouseClicked(event -> { // [cite: 72]
            if (event.getClickCount() == 2) { // [cite: 72]
                AdComponent selectedComponent = adListView.getSelectionModel().getSelectedItem(); // [cite: 72]
                if (selectedComponent != null && selectedComponent.getAd() != null) { // [cite: 72]
                    try {
                        MainGuiApp.loadAdDetailScene(selectedComponent.getAd()); // [cite: 73]
                    } catch (IOException e) {
                        showError("Помилка відкриття деталей оголошення: " + e.getMessage()); // [cite: 73]
                    }
                }
            }
        });
    }


    private void setupGlobalEventListeners() {
        if (categoryTreeView != null) { // [cite: 75]
            categoryTreeView.getSelectionModel().selectedItemProperty().addListener( // [cite: 75]
                    (observable, oldValue, newValueNode) -> {
                        if (newValueNode != null) { // [cite: 75]
                            CategoryComponent selectedCategory = newValueNode.getValue(); // [cite: 76]
                            if (selectedCategory != null && selectedCategory.getId() != null && selectedCategory.getName() != null) { // [cite: 76]
                                currentSelectedCategoryId = selectedCategory.getId(); // [cite: 76]
                                String categoryName = selectedCategory.getName(); // [cite: 77]
                                if (currentCategoryLabel != null) currentCategoryLabel.setText("Оголошення в категорії: " + categoryName); // [cite: 77]
                                if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: " + categoryName); // [cite: 78]

                                if (searchComponent != null) { // [cite: 79]
                                    searchComponent.updateCategory(currentSelectedCategoryId); // Inform mediator // [cite: 79]
                                    // EXPECT the mediator to handle ad loading and call controller.updateAdsList()
                                    // The direct call to loadAds() is removed from this path
                                    // to allow the mediator to control the data flow.
                                } else {
                                    // Fallback if mediator component is not available
                                    loadAds(currentSelectedCategoryId); // [cite: 81]
                                }
                            }
                        } else {
                            currentSelectedCategoryId = null; // [cite: 82]
                            if (currentCategoryLabel != null) currentCategoryLabel.setText("Всі оголошення"); // [cite: 83]
                            if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: немає"); // [cite: 83]
                            if (searchComponent != null) { // [cite: 84]
                                searchComponent.updateCategory(""); // Inform mediator // [cite: 84]
                                // EXPECT the mediator to handle ad loading
                            } else {
                                loadAds(null); // [cite: 85]
                            }
                        }
                    });
        }
    }


    private void setupQuickFilters() {
        if (quickFilterPremium != null) { // [cite: 88]
            quickFilterPremium.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters()); // [cite: 88]
        }
        if (quickFilterUrgent != null) { // [cite: 89]
            quickFilterUrgent.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters()); // [cite: 89]
        }
        if (quickFilterWithDelivery != null) { // [cite: 90]
            quickFilterWithDelivery.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters()); // [cite: 90]
        }
        if (quickFilterWithWarranty != null) { // [cite: 91]
            quickFilterWithWarranty.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters()); // [cite: 91]
        }
        if (quickFilterWithDiscount != null) { // [cite: 92]
            quickFilterWithDiscount.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters()); // [cite: 92]
        }
    }


    private void applyQuickFilters() {
        refreshCurrentView(); // [cite: 93]
        updateActiveFiltersDisplay(); // [cite: 94]
        updateStatus("Швидкі фільтри застосовано."); // [cite: 95]
    }


    private void initializeMediator() {
        if (adService == null || categoryService == null) { // [cite: 95]
            showErrorAlert("Критична помилка", "Сервіси не ініціалізовані.", "Неможливо створити медіатор."); // [cite: 95]
            updateMediatorStatus("помилка ініціалізації"); // [cite: 96]
            return; // [cite: 96]
        }
        mediator = new AdBrowserMediator(adService, categoryService); // [cite: 96]
        searchComponent = new SearchComponent(mediator); // [cite: 97]
        adListComponent = new AdListComponent(mediator); // [cite: 97]
        filterComponent = new FilterComponent(mediator); // [cite: 98]
        mediator.registerComponents(searchComponent, adListComponent, filterComponent); // [cite: 99]
        mediator.setController(this); // Allow mediator to call methods on controller like updateAdsList [cite: 99]

        System.out.println("Медіатор ініціалізовано успішно"); // [cite: 100]
        updateMediatorStatus("активний"); // [cite: 101]
    }


    private void setupMediatorIntegration() {
        if (searchField != null && searchComponent != null) { // [cite: 101]
            searchField.textProperty().addListener((observable, oldValue, newValue) -> { // [cite: 101]
                searchComponent.updateSearchText(newValue); // [cite: 101]
            });
            if (searchButton != null) { // [cite: 102]
                searchButton.setOnAction(e -> { // [cite: 102]
                    if (searchComponent != null) { // [cite: 103]
                        String searchText = (searchField != null) ? searchField.getText() : ""; // [cite: 103]
                        searchComponent.performSearch(searchText, currentSelectedCategoryId); // [cite: 103]
                        updateStatus("Пошук ініційовано через медіатор: " + searchText);
                    } else {
                        // Fallback if searchComponent is not available
                        handleSearchAds(); // [cite: 103]
                    }
                });
            }

        } else {
            if (searchField == null) System.err.println("searchField is null in setupMediatorIntegration"); // [cite: 105]
            if (searchComponent == null) System.err.println("searchComponent is null in setupMediatorIntegration"); // [cite: 106]
            if (searchButton != null) { // [cite: 106]
                searchButton.setOnAction(e -> handleSearchAds()); // [cite: 106]
            }
        }
    }


    // ========== ОБРОБНИКИ ПОДІЙ ==========


    @FXML
    private void handleToggleAdvancedSearch() {
        isAdvancedSearchVisible = !isAdvancedSearchVisible; // [cite: 107]
        if (advancedSearchPanel != null) { // [cite: 108]
            advancedSearchPanel.setVisible(isAdvancedSearchVisible); // [cite: 108]
            advancedSearchPanel.setManaged(isAdvancedSearchVisible); // [cite: 108]
        }
        updateStatus("Розширений пошук " + (isAdvancedSearchVisible ? "відкрито" : "закрито")); // [cite: 109]
    }


    @FXML
    private void handleApplyFilters() {
        showLoadingIndicator("Застосування фільтрів..."); // [cite: 110]
        String minPriceText = (minPriceField != null) ? minPriceField.getText() : ""; // [cite: 111]
        String maxPriceText = (maxPriceField != null) ? maxPriceField.getText() : ""; // [cite: 112]
        String selectedStatus = (statusFilterCombo != null && statusFilterCombo.getValue() != null) ? statusFilterCombo.getValue() : "Всі"; // [cite: 113]
        boolean premiumOnly = premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected(); // [cite: 114]
        boolean urgentOnly = urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected(); // [cite: 114]
        Double minPrice = null; // [cite: 115]
        Double maxPrice = null; // [cite: 115]
        try {
            if (!minPriceText.isEmpty()) { // [cite: 116]
                minPrice = Double.parseDouble(minPriceText); // [cite: 116]
            }
            if (!maxPriceText.isEmpty()) { // [cite: 117]
                maxPrice = Double.parseDouble(maxPriceText); // [cite: 117]
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Помилка фільтрації", "Невірний формат ціни", "Будь ласка, введіть коректні числові значення для ціни."); // [cite: 118]
            hideLoadingIndicator(); // [cite: 119]
            return; // [cite: 119]
        }

        String keyword = (searchField != null) ? searchField.getText() : ""; // [cite: 119]
        List<Ad> fetchedAds = adService.searchAds(keyword, minPrice, maxPrice, currentSelectedCategoryId); // [cite: 120]

        List<Ad> filteredAds = new ArrayList<>(); // [cite: 122]
        if (fetchedAds != null) { // [cite: 123]
            for (Ad ad : fetchedAds) { // [cite: 123]
                boolean statusMatch = "Всі".equals(selectedStatus) || (ad.getStatus() != null && selectedStatus.equals(ad.getStatus().toString())); // [cite: 123]
                if (!statusMatch) continue; // [cite: 124]
                if (premiumOnly && !ad.isPremium()) continue; // [cite: 126]
                if (urgentOnly && !ad.isUrgent()) continue; // [cite: 127]

                if (quickFilterPremium != null && quickFilterPremium.isSelected() && !ad.isPremium()) continue; // [cite: 127]
                if (quickFilterUrgent != null && quickFilterUrgent.isSelected() && !ad.isUrgent()) continue; // [cite: 128]
                // Add similar checks for other quick filters if they correspond to Ad properties
                // e.g., if (quickFilterWithDelivery.isSelected() && !ad.hasDelivery()) continue; [cite: 128]
                filteredAds.add(ad); // [cite: 129]
            }
        }


        List<AdComponent> decoratedAds = filteredAds.stream() // [cite: 129]
                .map(this::createDecoratedAd) // [cite: 129]
                .toList(); // [cite: 131]
        adsObservableList.setAll(decoratedAds); // [cite: 132]
        applySorting(); // Apply current sort order // [cite: 132]
        updateActiveFiltersDisplay(); // [cite: 132]
        updateStatistics(); // [cite: 132]
        updatePagination(); // [cite: 132]
        hideLoadingIndicator(); // [cite: 132]
        updateStatus("Фільтри застосовано. Знайдено " + decoratedAds.size() + " оголошень"); // [cite: 133]
    }


    @FXML
    private void handleClearFilters() {
        if (minPriceField != null) minPriceField.clear(); // [cite: 133]
        if (maxPriceField != null) maxPriceField.clear(); // [cite: 134]
        if (statusFilterCombo != null) statusFilterCombo.setValue("Всі"); // [cite: 134]
        if (premiumOnlyCheckBox != null) premiumOnlyCheckBox.setSelected(false); // [cite: 134]
        if (urgentOnlyCheckBox != null) urgentOnlyCheckBox.setSelected(false); // [cite: 134]
        refreshCurrentView(); // [cite: 136]
        updateActiveFiltersDisplay(); // [cite: 136]
        updateStatus("Фільтри розширеного пошуку очищено"); // [cite: 136]
    }


    @FXML
    private void handleClearAllFilters() {
        handleClearFilters(); // [cite: 137]

        if (quickFilterPremium != null) quickFilterPremium.setSelected(false); // [cite: 138]
        if (quickFilterUrgent != null) quickFilterUrgent.setSelected(false); // [cite: 139]
        if (quickFilterWithDelivery != null) quickFilterWithDelivery.setSelected(false); // [cite: 139]
        if (quickFilterWithWarranty != null) quickFilterWithWarranty.setSelected(false); // [cite: 139]
        if (quickFilterWithDiscount != null) quickFilterWithDiscount.setSelected(false); // [cite: 139]
        refreshCurrentView(); // [cite: 140]
        updateActiveFiltersDisplay(); // [cite: 141]
        updateStatus("Всі фільтри очищено"); // [cite: 141]
    }


    @FXML
    private void handleToggleSortOrder() {
        isAscendingSort = !isAscendingSort; // [cite: 142]
        if (sortOrderButton != null) { // [cite: 143]
            sortOrderButton.setText(isAscendingSort ? "↑" : "↓"); // [cite: 143]
        }
        applySorting(); // [cite: 144]
        updateStatus("Порядок сортування змінено на " + (isAscendingSort ? "зростаючий" : "спадаючий")); // [cite: 144]
    }


    private void handleSortChange() {
        if (sortComboBox == null || sortComboBox.getValue() == null) return; // [cite: 145]
        String selectedSort = sortComboBox.getValue(); // [cite: 146]
        switch (selectedSort) { // [cite: 146]
            case "За назвою": // [cite: 146]
                currentSortBy = "title"; // [cite: 146]
                break; // [cite: 147]
            case "За ціною": // [cite: 147]
                currentSortBy = "price"; // [cite: 147]
                break; // [cite: 148]
            case "За датою": // [cite: 148]
                currentSortBy = "date"; // [cite: 148]
                break; // [cite: 149]
            case "За популярністю": // Assuming 'popularity' is a sortable field in Ad // [cite: 149]
                currentSortBy = "popularity"; // [cite: 149]
                break; // [cite: 150]
            default: // [cite: 150]
                currentSortBy = "title"; // [cite: 150]
                break; // [cite: 151]
        }
        applySorting(); // [cite: 152]
        updateStatus("Сортування змінено на: " + selectedSort); // [cite: 152]
    }


    private void applySorting() {
        if (adsObservableList != null && !adsObservableList.isEmpty()) { // [cite: 153]
            adsObservableList.sort((ac1, ac2) -> { // [cite: 153]
                if (ac1 == null && ac2 == null) return 0; // [cite: 154]
                if (ac1 == null || ac1.getAd() == null) return isAscendingSort ? 1 : -1; // [cite: 154]
                if (ac2 == null || ac2.getAd() == null) return isAscendingSort ? -1 : 1; // [cite: 154]

                Ad ad1 = ac1.getAd(); // [cite: 154]
                Ad ad2 = ac2.getAd(); // [cite: 155]

                int comparisonResult = 0; // [cite: 155]
                switch (currentSortBy) { // [cite: 155]
                    case "title": // [cite: 155]
                        if (ad1.getTitle() != null && ad2.getTitle() != null) { // [cite: 156]
                            comparisonResult = ad1.getTitle().compareToIgnoreCase(ad2.getTitle()); // [cite: 156]
                        } else if (ad1.getTitle() == null && ad2.getTitle() != null) { // [cite: 156]
                            comparisonResult = -1; // [cite: 156]
                        } else if (ad1.getTitle() != null && ad2.getTitle() == null) { // [cite: 157]
                            comparisonResult = 1; // [cite: 157]
                        }
                        break; // [cite: 158]
                    case "price": // [cite: 159]
                        comparisonResult = Double.compare(ad1.getPrice(), ad2.getPrice()); // [cite: 159]
                        break; // [cite: 160]
                    case "date": // [cite: 160]
                        if (ad1.getCreatedAt() != null && ad2.getCreatedAt() != null) { // [cite: 160]
                            comparisonResult = ad1.getCreatedAt().compareTo(ad2.getCreatedAt()); // [cite: 160]
                        } else if (ad1.getCreatedAt() == null && ad2.getCreatedAt() != null) { // [cite: 161]
                            comparisonResult = -1; // [cite: 161]
                        } else if (ad1.getCreatedAt() != null && ad2.getCreatedAt() == null) { // [cite: 162]
                            comparisonResult = 1; // [cite: 162]
                        }
                        break; // [cite: 163]
                    case "popularity": // [cite: 164]
                        // Assuming Ad has a getPopularity() method returning a comparable type (e.g., int or double)
                        // comparisonResult = Double.compare(ad1.getPopularity(), ad2.getPopularity()); // [cite: 164]
                        // System.out.println("Sorting by popularity not yet fully implemented for Ad object."); // [cite: 165]
                        break; // [cite: 166]
                    default: // [cite: 166]
                        break; // [cite: 166]
                }
                return isAscendingSort ? comparisonResult : -comparisonResult; // [cite: 167]
            });
        }
        if (adListView != null) adListView.refresh(); // [cite: 168]
        System.out.println("Applying sort by: " + currentSortBy + (isAscendingSort ? " ASC" : " DESC")); // [cite: 169]
    }


    @FXML
    private void handleSwitchToListView() {
        if (listViewButton != null) listViewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // [cite: 170]
        if (gridViewButton != null) gridViewButton.setStyle(""); // [cite: 171]
        updateStatus("Перемкнуто на вигляд списку"); // [cite: 171]
    }


    @FXML
    private void handleSwitchToGridView() {
        if (gridViewButton != null) gridViewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // [cite: 172]
        if (listViewButton != null) listViewButton.setStyle(""); // [cite: 173]
        updateStatus("Перемкнуто на вигляд сітки"); // [cite: 173]
    }


    @FXML
    private void handleRefresh() {
        showLoadingIndicator("Оновлення..."); // [cite: 174]
        refreshCurrentView(); // [cite: 175]
        updateLastUpdateTime(); // [cite: 175]
        hideLoadingIndicator(); // [cite: 175]
        updateStatus("Список оновлено"); // [cite: 175]
    }


    // ========== ПАГІНАЦІЯ ==========


    @FXML
    private void handleFirstPage() {
        if (currentPage > 1) { // [cite: 176]
            currentPage = 1; // [cite: 176]
            refreshCurrentView(); // [cite: 177]
            updateStatus("Перехід на першу сторінку"); // [cite: 178]
        }
    }


    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) { // [cite: 179]
            currentPage--; // [cite: 179]
            refreshCurrentView(); // [cite: 180]
            updateStatus("Перехід на попередню сторінку"); // [cite: 180]
        }
    }


    @FXML
    private void handleNextPage() {
        int totalPages = getTotalPages(); // [cite: 180]
        if (currentPage < totalPages) { // [cite: 181]
            currentPage++; // [cite: 181]
            refreshCurrentView(); // [cite: 181]
            updateStatus("Перехід на наступну сторінку"); // [cite: 182]
        }
    }


    @FXML
    private void handleLastPage() {
        int totalPages = getTotalPages(); // [cite: 182]
        if (currentPage < totalPages) { // [cite: 183]
            currentPage = totalPages; // [cite: 183]
            refreshCurrentView(); // [cite: 183]
            updateStatus("Перехід на останню сторінку"); // [cite: 184]
        }
    }


    private void handlePageSizeChange() {
        if (pageSizeComboBox == null || pageSizeComboBox.getValue() == null) return; // [cite: 184]
        Integer newPageSize = pageSizeComboBox.getValue(); // [cite: 185]
        if (newPageSize != null && newPageSize > 0) { // [cite: 185]
            pageSize = newPageSize; // [cite: 185]
            currentPage = 1; // [cite: 186]
            refreshCurrentView(); // [cite: 186]
            updateStatus("Розмір сторінки змінено на " + pageSize); // [cite: 187]
        }
    }


    private void updatePaginationControls() {
        int totalItems = adsObservableList.size(); // [cite: 194]
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize)); // [cite: 195]
        if (currentPage > totalPages) { // [cite: 196]
            currentPage = totalPages; // [cite: 196]
        }
        if (currentPage < 1) { // [cite: 197]
            currentPage = 1; // [cite: 197]
        }

        if (pageInfoLabel != null) { // [cite: 198]
            pageInfoLabel.setText("Сторінка " + currentPage + " з " + totalPages); // [cite: 198]
        }

        if (firstPageButton != null) firstPageButton.setDisable(currentPage <= 1); // [cite: 199]
        if (prevPageButton != null) prevPageButton.setDisable(currentPage <= 1); // [cite: 200]
        if (nextPageButton != null) nextPageButton.setDisable(currentPage >= totalPages); // [cite: 200]
        if (lastPageButton != null) lastPageButton.setDisable(currentPage >= totalPages); // [cite: 201]

        if (paginationControls != null) { // [cite: 201]
            paginationControls.setVisible(totalPages > 1); // [cite: 201]
            paginationControls.setManaged(totalPages > 1); // [cite: 202]
        }

        if (adListView != null) { // [cite: 203]
            int fromIndex = (currentPage - 1) * pageSize; // [cite: 203]
            int toIndex = Math.min(fromIndex + pageSize, totalItems); // [cite: 204]

            if (fromIndex < totalItems) { // [cite: 204]
                List<AdComponent> pageData = adsObservableList.subList(fromIndex, toIndex); // [cite: 204]
                adListView.setItems(FXCollections.observableArrayList(pageData)); // [cite: 211]
            } else {
                adListView.setItems(FXCollections.emptyObservableList()); // [cite: 212]
            }
        }
    }

    private void updatePagination() {
        updatePaginationControls(); // [cite: 218]
    }



    private int getTotalPages() {
        if (adsObservableList == null || pageSize <= 0) return 1; // [cite: 219]
        int totalAds = adsObservableList.size(); // [cite: 220]
        return Math.max(1, (int) Math.ceil((double) totalAds / pageSize)); // [cite: 220]
    }


    // ========== ДОПОМІЖНІ МЕТОДИ ==========


    private void updateActiveFiltersDisplay() {
        if (activeFiltersContainer == null || activeFiltersPanel == null) return; // [cite: 221]
        activeFiltersContainer.getChildren().clear(); // [cite: 222]
        boolean hasActiveFilters = false; // [cite: 222]

        if (minPriceField != null && !minPriceField.getText().isEmpty()) { // [cite: 222]
            addFilterChip("Мін. ціна: " + minPriceField.getText()); // [cite: 222]
            hasActiveFilters = true; // [cite: 223]
        }
        if (maxPriceField != null && !maxPriceField.getText().isEmpty()) { // [cite: 223]
            addFilterChip("Макс. ціна: " + maxPriceField.getText()); // [cite: 223]
            hasActiveFilters = true; // [cite: 224]
        }
        if (statusFilterCombo != null && statusFilterCombo.getValue() != null && !"Всі".equals(statusFilterCombo.getValue())) { // [cite: 224]
            addFilterChip("Статус: " + statusFilterCombo.getValue()); // [cite: 224]
            hasActiveFilters = true; // [cite: 225]
        }
        if (premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected()) { // [cite: 225]
            addFilterChip("Тільки преміум (розш.)"); // [cite: 225]
            hasActiveFilters = true; // [cite: 226]
        }
        if (urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected()) { // [cite: 227]
            addFilterChip("Тільки терміново (розш.)"); // [cite: 227]
            hasActiveFilters = true; // [cite: 228]
        }

        if (quickFilterPremium != null && quickFilterPremium.isSelected()) { // [cite: 229]
            addFilterChip("⭐ Преміум"); // [cite: 229]
            hasActiveFilters = true; // [cite: 230]
        }
        if (quickFilterUrgent != null && quickFilterUrgent.isSelected()) { // [cite: 230]
            addFilterChip("🚨 Терміново"); // [cite: 230]
            hasActiveFilters = true; // [cite: 231]
        }
        if (quickFilterWithDelivery != null && quickFilterWithDelivery.isSelected()) { // [cite: 231]
            addFilterChip("🚚 З доставкою"); // [cite: 231]
            hasActiveFilters = true; // [cite: 232]
        }
        if (quickFilterWithWarranty != null && quickFilterWithWarranty.isSelected()) { // [cite: 232]
            addFilterChip("🛡️ З гарантією"); // [cite: 232]
            hasActiveFilters = true; // [cite: 233]
        }
        if (quickFilterWithDiscount != null && quickFilterWithDiscount.isSelected()) { // [cite: 233]
            addFilterChip("💰 Зі знижкою"); // [cite: 233]
            hasActiveFilters = true; // [cite: 234]
        }

        activeFiltersPanel.setVisible(hasActiveFilters); // [cite: 234]
        activeFiltersPanel.setManaged(hasActiveFilters); // [cite: 235]
    }


    private void addFilterChip(String text) {
        if (activeFiltersContainer != null) { // [cite: 235]
            Label filterChip = new Label(text); // [cite: 235]
            filterChip.getStyleClass().add("filter-chip"); // [cite: 236]
            HBox.setMargin(filterChip, new Insets(0, 5, 0, 0)); // [cite: 236]
            activeFiltersContainer.getChildren().add(filterChip); // [cite: 237]
        }
    }


    private void updateStatistics() {
        if (totalAdsLabel != null && adService != null) { // [cite: 237]
            if (adsObservableList != null) { // [cite: 241]
                totalAdsLabel.setText("Всього (фільтр.): " + adsObservableList.size()); // [cite: 241]
            }
        }

        if (filteredAdsLabel != null && adListView != null && adListView.getItems() != null) { // [cite: 242]
            filteredAdsLabel.setText("На сторінці: " + adListView.getItems().size()); // [cite: 242]
        }
        if (selectedCategoryLabel != null && currentSelectedCategoryId == null) { // [cite: 243]
            selectedCategoryLabel.setText("Обрана категорія: Всі"); // [cite: 243]
        }
    }


    private void updateMediatorStatus(String status) {
        if (mediatorStatusLabel != null) { // [cite: 244]
            mediatorStatusLabel.setText("Медіатор: " + status); // [cite: 244]
        }
    }


    private void updateLastUpdateTime() {
        if (lastUpdateLabel != null) { // [cite: 245]
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")); // [cite: 245]
            lastUpdateLabel.setText("Останнє оновлення: " + currentTime); // [cite: 246]
        }
    }


    private void updateStatus(String message) {
        if (statusLabel != null) { // [cite: 246]
            statusLabel.setText(message); // [cite: 246]
        }
    }


    private void showLoadingIndicator(String message) {
        if (loadingIndicator != null) { // [cite: 247]
            loadingIndicator.setVisible(true); // [cite: 247]
            loadingIndicator.setManaged(true); // [cite: 248]
        }
        if (loadingLabel != null) { // [cite: 248]
            loadingLabel.setText(message); // [cite: 248]
        }
    }


    private void hideLoadingIndicator() {
        if (loadingIndicator != null) { // [cite: 249]
            loadingIndicator.setVisible(false); // [cite: 249]
            loadingIndicator.setManaged(false); // [cite: 250]
        }
        if (loadingLabel != null) { // [cite: 250]
            loadingLabel.setText(""); // [cite: 250]
        }
    }


    // ========== ІСНУЮЧІ МЕТОДИ (БЕЗ ЗМІН, АЛЕ ПЕРЕВІРЕНІ) ==========


    private void initializeCommandManager() {
        if (adService == null) { // [cite: 251]
            showErrorAlert("Критична помилка", "AdService не ініціалізовано.", "Неможливо створити CommandManager."); // [cite: 251]
            return; // [cite: 252]
        }
        CommandInvoker commandInvoker = new CommandInvoker(); // [cite: 252]
        CommandFactory commandFactory = new CommandFactory(adService); // [cite: 252]
        commandManager = new AdCommandManager(commandInvoker, commandFactory); // [cite: 253]
    }


    private void setupCommandHistoryView() {
        if (commandHistoryListView != null) { // [cite: 254]
            commandHistoryListView.setItems(commandHistoryObservableList); // [cite: 254]
            commandHistoryListView.setPrefHeight(150); // [cite: 255]
        }
    }


    private void updateCommandButtons() {
        if (commandManager == null) return; // [cite: 255]
        if (undoButton != null) { // [cite: 256]
            undoButton.setDisable(!commandManager.canUndo()); // [cite: 256]
        }
        if (redoButton != null) { // [cite: 257]
            redoButton.setDisable(!commandManager.canRedo()); // [cite: 257]
        }
        if (commandHistoryObservableList != null) { // [cite: 258]
            commandHistoryObservableList.setAll(commandManager.getCommandHistory()); // [cite: 258]
        }
    }


    private void setupCategoryTree() {
        if (categoryTreeView == null || categoryService == null) { // [cite: 259]
            System.err.println("Error: categoryTreeView or categoryService is null. Cannot setup category tree."); // [cite: 259]
            if (categoryTreeView != null) { // [cite: 260]
                TreeItem<CategoryComponent> fallbackRoot = new TreeItem<>(new Category("fallback", "Помилка завантаження категорій", null)); // [cite: 260]
                categoryTreeView.setRoot(fallbackRoot); // [cite: 261]
            }
            return; // [cite: 261]
        }

        try {
            List<CategoryComponent> rootCategories = categoryService.getAllRootCategories(); // [cite: 262]
            if (rootCategories == null) { // [cite: 263]
                rootCategories = new ArrayList<>(); // [cite: 263]
                System.err.println("Warning: CategoryService.getAllRootCategories() returned null. Using empty list."); // [cite: 264]
            }

            Category allCategoriesRootNode = new Category("root", "Всі категорії", null); // [cite: 264]
            TreeItem<CategoryComponent> rootTreeItem = new TreeItem<>(allCategoriesRootNode); // [cite: 265]
            rootTreeItem.setExpanded(true); // [cite: 265]

            for (CategoryComponent rootCategory : rootCategories) { // [cite: 265]
                if (rootCategory != null) { // [cite: 265]
                    TreeItem<CategoryComponent> categoryItem = createTreeItem(rootCategory, true); // [cite: 265]
                    if (categoryItem != null) { // [cite: 266]
                        rootTreeItem.getChildren().add(categoryItem); // [cite: 266]
                    }
                } else {
                    System.err.println("Warning: Found null root category, skipping..."); // [cite: 267]
                }
            }

            categoryTreeView.setRoot(rootTreeItem); // [cite: 268]
            categoryTreeView.setShowRoot(true); // [cite: 269]

            categoryTreeView.setCellFactory(tv -> new TreeCell<CategoryComponent>() { // [cite: 269]
                @Override
                protected void updateItem(CategoryComponent item, boolean empty) {
                    super.updateItem(item, empty); // [cite: 269]
                    if (empty || item == null) { // [cite: 270]
                        setText(null); // [cite: 270]
                    } else {
                        String name = item.getName(); // [cite: 270]
                        setText(name != null ? name : "Невідома категорія"); // [cite: 271]
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error setting up category tree: " + e.getMessage()); // [cite: 272]
            e.printStackTrace(); // [cite: 273]
            Category fallbackRootData = new Category("error_root", "Помилка завантаження", null); // [cite: 273]
            TreeItem<CategoryComponent> fallbackItem = new TreeItem<>(fallbackRootData); // [cite: 274]
            fallbackItem.setExpanded(true); // [cite: 274]
            categoryTreeView.setRoot(fallbackItem); // [cite: 274]
            categoryTreeView.setShowRoot(true); // [cite: 274]
        }
    }

    private TreeItem<CategoryComponent> createTreeItem(CategoryComponent categoryComponent, boolean autoExpand) {
        if (categoryComponent == null) { // [cite: 275]
            System.err.println("Warning: Attempting to create TreeItem for null categoryComponent"); // [cite: 275]
            return null; // [cite: 276]
        }

        TreeItem<CategoryComponent> item = new TreeItem<>(categoryComponent); // [cite: 276]
        item.setExpanded(autoExpand); // [cite: 276]
        if (categoryComponent instanceof Category) { // [cite: 277]
            Category category = (Category) categoryComponent; // [cite: 277]
            CategoryComponent[] childrenArray = category.getChildren(); // [cite: 278]
            if (childrenArray != null) { // [cite: 278]
                List<CategoryComponent> children = List.of(childrenArray); // [cite: 278]
                if (!children.isEmpty()) { // [cite: 279]
                    for (CategoryComponent child : children) { // [cite: 279]
                        if (child != null) { // [cite: 279]
                            TreeItem<CategoryComponent> childItem = createTreeItem(child, false); // [cite: 279]
                            if (childItem != null) { // [cite: 280]
                                item.getChildren().add(childItem); // [cite: 280]
                            }
                        } else {
                            System.err.println("Warning: Found null child category in category: " + (category.getName() != null ? category.getName() : "ID: "+category.getId())); // [cite: 281]
                        }
                    }
                }
            }
        }
        return item; // [cite: 283]
    }

    /*
    // Commented out original setupAdListView - the new one is above and uses AdComponent
    private void setupAdListView() {
        if (adListView != null) {
            adListView.setItems(adsObservableList); [cite: 283]
            adListView.setCellFactory(new Callback<ListView<AdComponent>, ListCell<AdComponent>>() { [cite: 284]
                @Override
                public ListCell<AdComponent> call(ListView<AdComponent> param) {
                    return new ListCell<AdComponent>() { [cite: 284]
                        @Override
                        protected void updateItem(AdComponent item, boolean empty) { [cite: 285]
                            super.updateItem(item, empty);
                            if (empty || item == null) { [cite: 285]
                                setText(null); [cite: 286]
                                setGraphic(null);
                            } else {
                                setText(item.getDisplayInfo()); [cite: 287]
                                setOnMouseClicked(event -> {
                                    if (event.getClickCount() == 2) { [cite: 287]
                                        handleOpenAdDetails(item.getAd()); [cite: 288]
                                    }
                                });
                            }
                        }
                    };
                } [cite: 290]
            });
        } [cite: 291]
    }
    */

    private void validateCategoryData() {
        if (categoryService == null) return; // [cite: 292]
        try {
            List<CategoryComponent> rootCategories = categoryService.getAllRootCategories(); // [cite: 293]
            if (rootCategories == null) { // [cite: 294]
                System.err.println("VALIDATION ERROR: getAllRootCategories() returned null"); // [cite: 294]
                return; // [cite: 295]
            }
            System.out.println("Validating " + rootCategories.size() + " root categories..."); // [cite: 295]
            for (int i = 0; i < rootCategories.size(); i++) { // [cite: 296]
                CategoryComponent category = rootCategories.get(i); // [cite: 296]
                if (category == null) { // [cite: 297]
                    System.err.println("VALIDATION ERROR: Root category at index " + i + " is null"); // [cite: 297]
                    continue; // [cite: 298]
                }
                validateCategory(category, "Root[" + i + "]"); // [cite: 298]
            }
            System.out.println("Category validation completed."); // [cite: 299]
        } catch (Exception e) {
            System.err.println("Error during category validation: " + e.getMessage()); // [cite: 300]
            e.printStackTrace(); // [cite: 301]
        }
    }

    private void validateCategory(CategoryComponent category, String path) {
        if (category == null) { // [cite: 301]
            System.err.println("VALIDATION ERROR: Category is null at path: " + path); // [cite: 301]
            return; // [cite: 302]
        }
        String name = category.getName(); // [cite: 302]
        String id = category.getId(); // [cite: 302]
        if (name == null) { // [cite: 303]
            System.err.println("VALIDATION WARNING: Category name is null at path: " + path + " (ID: " + id + ")"); // [cite: 303]
        }
        if (id == null) { // [cite: 304]
            System.err.println("VALIDATION WARNING: Category id is null at path: " + path + " (Name: " + name + ")"); // [cite: 304]
        }

        if (category instanceof Category) { // [cite: 305]
            Category cat = (Category) category; // [cite: 305]
            CategoryComponent[] childrenArray = cat.getChildren(); // [cite: 306]
            if (childrenArray != null) { // [cite: 306]
                List<CategoryComponent> children = List.of(childrenArray); // [cite: 306]
                for (int i = 0; i < children.size(); i++) { // [cite: 307]
                    CategoryComponent child = children.get(i); // [cite: 307]
                    validateCategory(child, path + " -> " + (name != null ? name : "null_name") + "[" + i + "]"); // [cite: 308]
                }
            }
        }
    }


    private void loadAds(String categoryId) {
        if (adService == null) { // [cite: 310]
            showErrorAlert("Помилка", "Сервіс оголошень недоступний.", "Неможливо завантажити оголошення."); // [cite: 310]
            return; // [cite: 311]
        }
        showLoadingIndicator("Завантаження оголошень..."); // [cite: 311]
        List<Ad> ads; // [cite: 311]
        String keyword = searchField != null ? searchField.getText() : ""; // [cite: 312]
        ads = adService.searchAds(keyword, null, null, categoryId); // [cite: 313]

        List<Ad> filteredByQuickFilters = new ArrayList<>(); // [cite: 313]
        if (ads != null) { // [cite: 314]
            for (Ad ad : ads) { // [cite: 314]
                boolean pass = true; // [cite: 314]
                if (quickFilterPremium != null && quickFilterPremium.isSelected() && !ad.isPremium()) pass = false; // [cite: 315]
                if (quickFilterUrgent != null && quickFilterUrgent.isSelected() && !ad.isUrgent()) pass = false; // [cite: 316]
                if (quickFilterWithDelivery != null && quickFilterWithDelivery.isSelected() && !ad.hasDelivery()) pass = false; // [cite: 317]
                if (quickFilterWithWarranty != null && quickFilterWithWarranty.isSelected() && !ad.hasWarranty()) pass = false; // [cite: 318]
                if (quickFilterWithDiscount != null && quickFilterWithDiscount.isSelected() && !ad.hasDiscount()) pass = false; // [cite: 319]

                if (pass) { // [cite: 320]
                    filteredByQuickFilters.add(ad); // [cite: 320]
                }
            }
        }


        List<AdComponent> decoratedAds = filteredByQuickFilters.stream() // [cite: 321]
                .map(this::createDecoratedAd) // [cite: 321]
                .toList(); // [cite: 322]
        adsObservableList.setAll(decoratedAds); // [cite: 322]

        applySorting(); // [cite: 322]
        updatePaginationControls(); // [cite: 323]
        updateStatistics(); // [cite: 324]
        hideLoadingIndicator(); // [cite: 325]
        updateStatus("Завантажено " + adsObservableList.size() + " відповідних оголошень (до пагінації). На сторінці: " + (adListView.getItems() != null ? adListView.getItems().size() : 0) ); // [cite: 326]
    }



    private void refreshCurrentView() {
        loadAds(currentSelectedCategoryId); // [cite: 331]
    }

    private void handleOpenAdDetails(Ad ad) {
        if (ad == null) return; // [cite: 332]
        try {
            MainGuiApp.loadAdDetailScene(ad); // [cite: 333]
        } catch (IOException e) {
            e.printStackTrace(); // [cite: 334]
            showErrorAlert("Помилка", "Не вдалося відкрити деталі оголошення", e.getMessage()); // [cite: 335]
        }
    }

    @FXML
    private void handleSearchAds() {
        // This method might serve as a fallback or an alternative trigger.
        // refreshCurrentView() handles loading ads based on the current searchField text and other filters.
        refreshCurrentView(); // [cite: 337]
        if (searchField != null) { // [cite: 337]
            updateStatus("Пошук за запитом: " + searchField.getText()); // [cite: 338]
        }
        // Removed redundant call to searchComponent.performSearch() here as refreshCurrentView already handles the search criteria.
    }


    @FXML
    private void handleCreateAd() {
        try {
            MainGuiApp.loadCreateAdScene(); // [cite: 339]
            Platform.runLater(() -> { // [cite: 342]
                refreshCurrentView(); // [cite: 342]
                updateStatus("Список оновлено після можливого створення оголошення."); // [cite: 343]
            });
        } catch (IOException e) {
            e.printStackTrace(); // [cite: 344]
            showErrorAlert("Помилка", "Не вдалося відкрити форму створення оголошення", e.getMessage()); // [cite: 345]
        }
    }

    @FXML
    private void handleLogout() {
        GlobalContext.getInstance().setLoggedInUser(null); // [cite: 345]
        try {
            MainGuiApp.loadLoginScene(); // [cite: 346]
        } catch (IOException e) {
            e.printStackTrace(); // [cite: 347]
            showErrorAlert("Помилка виходу", "Не вдалося завантажити сторінку входу.", e.getMessage()); // [cite: 348]
            Platform.exit(); // [cite: 348]
        }
    }

    @FXML
    private void handleExitApplication() {
        Optional<ButtonType> result = showConfirmationAlert( // [cite: 348]
                "Підтвердження виходу", // [cite: 348]
                "Ви впевнені, що хочете закрити програму?", // [cite: 349]
                "Всі незбережені зміни можуть бути втрачені." // [cite: 349]
        );
        if (result.isPresent() && result.get() == ButtonType.OK) { // [cite: 349]
            Platform.exit(); // [cite: 349]
        }
    }

    // ========== COMMAND PATTERN HANDLERS ==========

    @FXML
    private void handleUndo() {
        if (commandManager == null) return; // [cite: 350]
        if (commandManager.canUndo()) { // [cite: 351]
            try {
                commandManager.undo(); // [cite: 351]
                refreshCurrentView(); // [cite: 352]
                updateCommandButtons(); // [cite: 352]
                updateStatus("Команда скасована"); // [cite: 353]
            } catch (UserNotFoundException e) {
                showErrorAlert("Помилка скасування", "Не вдалося скасувати команду.", e.getMessage()); // [cite: 353]
            }
        }
    }

    @FXML
    private void handleRedo() {
        if (commandManager == null) return; // [cite: 354]
        if (commandManager.canRedo()) { // [cite: 355]
            try {
                commandManager.redo(); // [cite: 355]
                refreshCurrentView(); // [cite: 356]
                updateCommandButtons(); // [cite: 356]
                updateStatus("Команда повторена"); // [cite: 357]
            } catch (UserNotFoundException e) {
                showErrorAlert("Помилка повторення", "Не вдалося повторити команду.", e.getMessage()); // [cite: 357]
            }
        }
    }

    @FXML
    private void handleClearHistory() {
        if (commandManager == null) return; // [cite: 358]
        Optional<ButtonType> result = showConfirmationAlert( // [cite: 359]
                "Очистити історію команд", // [cite: 359]
                "Ви впевнені, що хочете очистити історію команд?", // [cite: 359]
                "Цю дію неможливо скасувати." // [cite: 359]
        );
        if (result.isPresent() && result.get() == ButtonType.OK) { // [cite: 360]
            commandManager.clearHistory(); // [cite: 360]
            updateCommandButtons(); // [cite: 360]
            updateStatus("Історія команд очищена"); // [cite: 361]
        }
    }

    // ========== UTILITY METHODS ==========

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR); // [cite: 361]
        alert.setTitle("Помилка"); // [cite: 362]
        alert.setHeaderText(null); // [cite: 362]
        alert.setContentText(message); // [cite: 362]
        alert.showAndWait(); // [cite: 362]
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR); // [cite: 362]
        alert.setTitle(title); // [cite: 363]
        alert.setHeaderText(header); // [cite: 363]
        alert.setContentText(content); // [cite: 363]
        alert.showAndWait(); // [cite: 363]
    }

    private Optional<ButtonType> showConfirmationAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // [cite: 363]
        alert.setTitle(title); // [cite: 364]
        alert.setHeaderText(header); // [cite: 364]
        alert.setContentText(content); // [cite: 364]
        return alert.showAndWait(); // [cite: 364]
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // [cite: 364]
        alert.setTitle(title); // [cite: 365]
        alert.setHeaderText(header); // [cite: 365]
        alert.setContentText(content); // [cite: 365]
        alert.showAndWait(); // [cite: 365]
    }

    private AdComponent createDecoratedAd(Ad ad) {
        if (ad == null) return null; // [cite: 365]
        AdDecoratorFactory decoratorFactory = new AdDecoratorFactory(); // [cite: 366]
        AdComponent baseComponent = decoratorFactory.createBaseAdComponent(ad); // [cite: 367]

        if (ad.isPremium()) { // [cite: 367]
            baseComponent = decoratorFactory.createPremiumAd((Ad) baseComponent); // CORRECTED: Assuming createPremiumAd takes AdComponent [cite: 368]
        }
        if (ad.isUrgent()) { // [cite: 368]
            baseComponent = decoratorFactory.createUrgentAd((Ad) baseComponent); // CORRECTED: Assuming createUrgentAd takes AdComponent [cite: 369]
        }
        if (ad.hasDiscount()) { // [cite: 369]
            baseComponent = decoratorFactory.createDiscountAd(baseComponent); // [cite: 369]
        }
        // if (ad.hasDelivery()) { baseComponent = decoratorFactory.createDeliveryAd(baseComponent); } // [cite: 370]
        // if (ad.hasWarranty()) { baseComponent = decoratorFactory.createWarrantyAd(baseComponent); } // [cite: 371]

        return baseComponent; // [cite: 373]
    }

    // ========== MEDIATOR INTEGRATION METHODS ==========

    public void updateAdsList(List<Ad> adsFromMediator) {
        if (adsFromMediator == null) adsFromMediator = new ArrayList<>(); // [cite: 376]
        List<AdComponent> decoratedAds = adsFromMediator.stream() // [cite: 376]
                .map(this::createDecoratedAd) // [cite: 376]
                .filter(java.util.Objects::nonNull) // [cite: 376]
                .toList(); // [cite: 377]
        Platform.runLater(() -> { // [cite: 377]
            adsObservableList.setAll(decoratedAds); // [cite: 377]
            applySorting(); // [cite: 377]
            updatePaginationControls(); // [cite: 377]
            updateStatistics(); // [cite: 377]
            updateStatus("Список оновлено через медіатор. " + decoratedAds.size() + " оголошень."); // [cite: 377]
            hideLoadingIndicator(); // [cite: 378]
        });
    }

    public void updateMediatorMessage(String message) {
        Platform.runLater(() -> { // [cite: 379]
            updateStatus(message); // [cite: 380]
            updateMediatorStatus("активний (повідомлення)"); // [cite: 380]
        });
    }

    public User getCurrentUser() {
        return GlobalContext.getInstance().getLoggedInUser(); // [cite: 381]
    }

    public void logAction(String action) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")); // [cite: 381]
        String logMessage = "[" + timestamp + "] " + action; // [cite: 382]
        Platform.runLater(() -> { // [cite: 383]
            if (commandHistoryObservableList != null) { // [cite: 383]
                if (commandHistoryObservableList.size() > 100) { // [cite: 383]
                    commandHistoryObservableList.remove(0); // [cite: 383]
                }
                commandHistoryObservableList.add(logMessage); // [cite: 383]
                if (commandHistoryListView != null && !commandHistoryObservableList.isEmpty()) { // [cite: 384]
                    commandHistoryListView.scrollTo(commandHistoryObservableList.size() - 1); // [cite: 384]
                }
            }
        });
    }

    // ========== CLEANUP ==========

    public void cleanup() {
        updateStatus("Очищення контролера..."); // [cite: 386]
        if (mediator != null) { // [cite: 387]
            updateMediatorStatus("неактивний (очищено)"); // [cite: 387]
        }

        System.out.println("MainController cleanup finished."); // [cite: 392]
    }
}
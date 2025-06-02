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

    // Додаємо компоненти медіатора
    private AdBrowserMediator mediator;
    private SearchComponent searchComponent;
    private AdListComponent adListComponent; // Although declared, not directly used in this controller for its methods
    private FilterComponent filterComponent;

    // Додаткові змінні для пагінації та сортування
    private int currentPage = 1;
    private int pageSize = 20; // Default page size
    private boolean isAscendingSort = true;
    private String currentSortBy = "title"; // Default sort
    private boolean isAdvancedSearchVisible = false;

    @FXML
    public void initialize() {
        User currentUser = GlobalContext.getInstance().getLoggedInUser(); // [cite: 52]
        if (currentUser != null) { // [cite: 53]
            loggedInUserLabel.setText("Користувач: " + currentUser.getUsername()); // [cite: 53]
            createAdButton.setDisable(false); // [cite: 54]
            logoutButton.setDisable(false); // [cite: 55]
        } else {
            try {
                MainGuiApp.loadLoginScene(); // [cite: 56]
                return; // Stop further initialization if not logged in // [cite: 57]
            } catch (IOException e) {
                e.printStackTrace(); // [cite: 58]
                showErrorAlert("Помилка входу", "Не вдалося завантажити сторінку входу.", e.getMessage()); // [cite: 59]
                Platform.exit(); // Exit if login scene fails to load // [cite: 60]
                return; // [cite: 61]
            }
        }

        initializeCommandManager(); // [cite: 62]
        initializeMediator(); // [cite: 63]
        initializeUIComponents(); // [cite: 64]
        setupCategoryTree(); // [cite: 65]
        setupAdListView(); // [cite: 66]
        setupCommandHistoryView(); // [cite: 67]
        setupMediatorIntegration(); // [cite: 68]
        setupGlobalEventListeners(); // [cite: 69]

        // Initial data load via mediator and UI updates
        if (this.mediator != null) { // [cite: 70]
            this.mediator.loadAllAds(); // [cite: 70]
        } else {
            // Fallback or direct load if mediator is not central to initial load
            loadAds(null); // [cite: 71]
        }

        updateCommandButtons(); // [cite: 72]
        updateLastUpdateTime(); // [cite: 73]
        updateStatus("Головне вікно ініціалізовано."); // [cite: 74]
    }

    private void initializeUIComponents() {
        // Initialize ComboBoxes
        ObservableList<String> sortOptions = FXCollections.observableArrayList( // [cite: 75]
                "За назвою", "За ціною", "За датою", "За популярністю" // Match with handleSortChange cases // [cite: 75]
        );
        if (sortComboBox != null) { // [cite: 76]
            sortComboBox.setItems(sortOptions); // [cite: 76]
            sortComboBox.setValue("За назвою"); // [cite: 77]
            sortComboBox.setOnAction(e -> handleSortChange()); // [cite: 78]
        }

        ObservableList<String> statusOptions = FXCollections.observableArrayList( // [cite: 79]
                "Всі", "Активне", "Чернетка", "Архівоване", "Продано" // [cite: 79]
        );
        if (statusFilterCombo != null) { // [cite: 80]
            statusFilterCombo.setItems(statusOptions); // [cite: 80]
            statusFilterCombo.setValue("Всі"); // [cite: 81]
        }

        ObservableList<Integer> pageSizeOptions = FXCollections.observableArrayList( // [cite: 82]
                10, 20, 50, 100 // [cite: 82]
        );
        if (pageSizeComboBox != null) { // [cite: 83]
            pageSizeComboBox.setItems(pageSizeOptions); // [cite: 83]
            pageSizeComboBox.setValue(pageSize); // [cite: 84]
            pageSizeComboBox.setOnAction(e -> handlePageSizeChange()); // [cite: 85]
        }

        setupQuickFilters(); // [cite: 86]

        if (advancedSearchPanel != null) { // [cite: 87]
            advancedSearchPanel.setVisible(false); // [cite: 87]
            advancedSearchPanel.setManaged(false); // [cite: 88]
        }
    }

    private void setupAdListView() {
        if (adListView == null) { // [cite: 89]
            System.err.println("Error: adListView is null. Check FXML binding."); // [cite: 89]
            return; // [cite: 90]
        }
        adListView.setItems(adsObservableList); // [cite: 91]

        adListView.setCellFactory(listView -> new ListCell<AdComponent>() { // [cite: 92]
            @Override
            protected void updateItem(AdComponent adComponent, boolean empty) {
                super.updateItem(adComponent, empty);

                if (empty || adComponent == null || adComponent.getAd() == null) { // [cite: 92]
                    setText(null); // [cite: 92]
                    setGraphic(null); // [cite: 92]
                } else {
                    Ad ad = adComponent.getAd(); // [cite: 92]

                    VBox container = new VBox(5); // [cite: 93]
                    container.setPadding(new Insets(10)); // [cite: 93]

                    Label titleLabel = new Label(ad.getTitle()); // [cite: 93]
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;"); // [cite: 93]

                    Label priceLabel = new Label(String.format("%.2f грн", ad.getPrice())); // [cite: 94]
                    priceLabel.setStyle("-fx-text-fill: #2E8B57; -fx-font-weight: bold;"); // [cite: 95]

                    String description = ad.getDescription(); // [cite: 96]
                    if (description != null && description.length() > 100) { // [cite: 96]
                        description = description.substring(0, 100) + "..."; // [cite: 96]
                    }
                    Label descLabel = new Label(description != null ? description : "Немає опису"); // [cite: 96]
                    descLabel.setStyle("-fx-text-fill: #666666;"); // [cite: 96]

                    HBox infoBox = new HBox(15); // [cite: 96]
                    String categoryName = "Невідомо"; // [cite: 97]
                    if (ad.getCategoryId() != null) { // [cite: 97]
                        categoryName = "ID: " + ad.getCategoryId(); // [cite: 98]
                    }
                    Label categoryInfoLabel = new Label("Категорія: " + categoryName); // [cite: 98]
                    String dateStr = "Дата: невідома"; // [cite: 99]
                    if (ad.getCreatedAt() != null) { // [cite: 99]
                        try {
                            dateStr = "Дата: " + ad.getCreatedAt().toLocalDate().toString(); // [cite: 100]
                        } catch (Exception e) {
                            dateStr = "Дата: " + java.time.LocalDate.now().toString() + " (fallback)"; // [cite: 101]
                        }
                    }
                    Label dateLabel = new Label(dateStr); // [cite: 102]
                    infoBox.getChildren().addAll(categoryInfoLabel, dateLabel); // [cite: 103]

                    Label decoratedInfoLabel = new Label(adComponent.getDisplayInfo()); // [cite: 104]
                    decoratedInfoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: blue;"); // [cite: 105]

                    container.getChildren().addAll(titleLabel, priceLabel, descLabel, infoBox, decoratedInfoLabel); // [cite: 106]
                    setGraphic(container); // [cite: 107]
                }
            }
        });
        adListView.setOnMouseClicked(event -> { // [cite: 108]
            if (event.getClickCount() == 2) { // [cite: 108]
                AdComponent selectedComponent = adListView.getSelectionModel().getSelectedItem(); // [cite: 108]
                if (selectedComponent != null && selectedComponent.getAd() != null) { // [cite: 108]
                    try {
                        MainGuiApp.loadAdDetailScene(selectedComponent.getAd()); // [cite: 109]
                    } catch (IOException e) {
                        showError("Помилка відкриття деталей оголошення: " + e.getMessage()); // [cite: 109]
                    }
                }
            }
        });
    }


    private void setupGlobalEventListeners() {
        if (categoryTreeView != null) { // [cite: 112]
            categoryTreeView.getSelectionModel().selectedItemProperty().addListener( // [cite: 112]
                    (observable, oldValue, newValueNode) -> {
                        if (newValueNode != null) { // [cite: 112]
                            CategoryComponent selectedCategory = newValueNode.getValue(); // [cite: 112]
                            if (selectedCategory != null && selectedCategory.getId() != null && selectedCategory.getName() != null) { // [cite: 112]
                                currentSelectedCategoryId = selectedCategory.getId(); // [cite: 113]
                                String categoryName = selectedCategory.getName(); // [cite: 114]
                                if (currentCategoryLabel != null) currentCategoryLabel.setText("Оголошення в категорії: " + categoryName); // [cite: 114]
                                if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: " + categoryName); // [cite: 115]

                                if (searchComponent != null) { // [cite: 116]
                                    searchComponent.updateCategory(currentSelectedCategoryId); // Inform mediator // [cite: 116]
                                    // EXPECT the mediator to handle ad loading and call controller.updateAdsList()
                                    // The direct call to loadAds() is removed from this path
                                    // to allow the mediator to control the data flow.
                                } else {
                                    // Fallback if mediator component is not available
                                    loadAds(currentSelectedCategoryId); // [cite: 119]
                                }
                            }
                        } else {
                            currentSelectedCategoryId = null; // [cite: 120]
                            if (currentCategoryLabel != null) currentCategoryLabel.setText("Всі оголошення"); // [cite: 122]
                            if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: немає"); // [cite: 123]
                            if (searchComponent != null) { // [cite: 124]
                                searchComponent.updateCategory(""); // Inform mediator // [cite: 124]
                                // EXPECT the mediator to handle ad loading
                            } else {
                                loadAds(null); // [cite: 125]
                            }
                        }
                    });
        }
    }


    private void setupQuickFilters() {
        if (quickFilterPremium != null) { // [cite: 127]
            quickFilterPremium.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters()); // [cite: 127]
        }
        if (quickFilterUrgent != null) { // [cite: 128]
            quickFilterUrgent.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters()); // [cite: 128]
        }
        if (quickFilterWithDelivery != null) { // [cite: 129]
            quickFilterWithDelivery.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters()); // [cite: 129]
        }
        if (quickFilterWithWarranty != null) { // [cite: 130]
            quickFilterWithWarranty.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters()); // [cite: 130]
        }
        if (quickFilterWithDiscount != null) { // [cite: 131]
            quickFilterWithDiscount.selectedProperty().addListener((obs, old, selected) -> applyQuickFilters()); // [cite: 131]
        }
    }


    private void applyQuickFilters() {
        refreshCurrentView(); // [cite: 132]
        updateActiveFiltersDisplay(); // [cite: 133]
        updateStatus("Швидкі фільтри застосовано."); // [cite: 134]
    }


    private void initializeMediator() {
        if (adService == null || categoryService == null) { // [cite: 135]
            showErrorAlert("Критична помилка", "Сервіси не ініціалізовані.", "Неможливо створити медіатор."); // [cite: 135]
            updateMediatorStatus("помилка ініціалізації"); // [cite: 136]
            return; // [cite: 137]
        }
        mediator = new AdBrowserMediator(adService, categoryService); // [cite: 138]
        searchComponent = new SearchComponent(mediator); // [cite: 139]
        adListComponent = new AdListComponent(mediator); // [cite: 140]
        filterComponent = new FilterComponent(mediator); // [cite: 141]
        mediator.registerComponents(searchComponent, adListComponent, filterComponent); // [cite: 142]
        mediator.setController(this); // Allow mediator to call methods on controller like updateAdsList // [cite: 143]

        System.out.println("Медіатор ініціалізовано успішно"); // [cite: 144]
        updateMediatorStatus("активний"); // [cite: 145]
    }


    private void setupMediatorIntegration() {
        if (searchField != null && searchComponent != null) { // [cite: 146]
            searchField.textProperty().addListener((observable, oldValue, newValue) -> { // [cite: 146]
                searchComponent.updateSearchText(newValue); // [cite: 146]
            });
            if (searchButton != null) { // [cite: 147]
                searchButton.setOnAction(e -> { // [cite: 147]
                    if (searchComponent != null) { // [cite: 147]
                        String searchText = (searchField != null) ? searchField.getText() : ""; // [cite: 147]
                        searchComponent.performSearch(searchText, currentSelectedCategoryId); // [cite: 147]
                        updateStatus("Пошук ініційовано через медіатор: " + searchText);
                    } else {
                        // Fallback if searchComponent is not available
                        handleSearchAds(); // [cite: 148]
                    }
                });
            }

        } else {
            if (searchField == null) System.err.println("searchField is null in setupMediatorIntegration"); // [cite: 150]
            if (searchComponent == null) System.err.println("searchComponent is null in setupMediatorIntegration"); // [cite: 151]
            if (searchButton != null) { // [cite: 152]
                searchButton.setOnAction(e -> handleSearchAds()); // [cite: 152]
            }
        }
    }


    // ========== ОБРОБНИКИ ПОДІЙ ==========


    @FXML
    private void handleToggleAdvancedSearch() {
        isAdvancedSearchVisible = !isAdvancedSearchVisible; // [cite: 153]
        if (advancedSearchPanel != null) { // [cite: 154]
            advancedSearchPanel.setVisible(isAdvancedSearchVisible); // [cite: 154]
            advancedSearchPanel.setManaged(isAdvancedSearchVisible); // [cite: 155]
        }
        updateStatus("Розширений пошук " + (isAdvancedSearchVisible ? "відкрито" : "закрито")); // [cite: 156]
    }


    @FXML
    private void handleApplyFilters() {
        showLoadingIndicator("Застосування фільтрів..."); // [cite: 157]
        String minPriceText = (minPriceField != null) ? minPriceField.getText() : ""; // [cite: 158]
        String maxPriceText = (maxPriceField != null) ? maxPriceField.getText() : ""; // [cite: 159]
        String selectedStatus = (statusFilterCombo != null && statusFilterCombo.getValue() != null) ? statusFilterCombo.getValue() : "Всі"; // [cite: 160]
        boolean premiumOnly = premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected(); // [cite: 161]
        boolean urgentOnly = urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected(); // [cite: 162]
        Double minPrice = null; // [cite: 163]
        Double maxPrice = null; // [cite: 164]
        try {
            if (!minPriceText.isEmpty()) { // [cite: 165]
                minPrice = Double.parseDouble(minPriceText); // [cite: 165]
            }
            if (!maxPriceText.isEmpty()) { // [cite: 166]
                maxPrice = Double.parseDouble(maxPriceText); // [cite: 166]
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Помилка фільтрації", "Невірний формат ціни", "Будь ласка, введіть коректні числові значення для ціни."); // [cite: 167]
            hideLoadingIndicator(); // [cite: 168]
            return; // [cite: 169]
        }

        String keyword = (searchField != null) ? searchField.getText() : ""; // [cite: 170]
        List<Ad> fetchedAds = adService.searchAds(keyword, minPrice, maxPrice, currentSelectedCategoryId); // [cite: 171]

        List<Ad> filteredAds = new ArrayList<>(); // [cite: 172]
        if (fetchedAds != null) { // [cite: 172]
            for (Ad ad : fetchedAds) { // [cite: 172]
                boolean statusMatch = "Всі".equals(selectedStatus) || (ad.getStatus() != null && selectedStatus.equals(ad.getStatus().toString())); // [cite: 173]
                if (!statusMatch) continue; // [cite: 174]
                if (premiumOnly && !ad.isPremium()) continue; // [cite: 175]
                if (urgentOnly && !ad.isUrgent()) continue; // [cite: 176]

                if (quickFilterPremium != null && quickFilterPremium.isSelected() && !ad.isPremium()) continue; // [cite: 177]
                if (quickFilterUrgent != null && quickFilterUrgent.isSelected() && !ad.isUrgent()) continue; // [cite: 178]
                // Add similar checks for other quick filters if they correspond to Ad properties
                // e.g., if (quickFilterWithDelivery.isSelected() && !ad.hasDelivery()) continue; // [cite: 179]
                filteredAds.add(ad); // [cite: 180]
            }
        }


        List<AdComponent> decoratedAds = filteredAds.stream() // [cite: 181]
                .map(this::createDecoratedAd) // [cite: 181]
                .toList(); // [cite: 181]
        adsObservableList.setAll(decoratedAds); // [cite: 182]
        applySorting(); // Apply current sort order // [cite: 183]
        updateActiveFiltersDisplay(); // [cite: 184]
        updateStatistics(); // [cite: 185]
        updatePagination(); // [cite: 186]
        hideLoadingIndicator(); // [cite: 187]
        updateStatus("Фільтри застосовано. Знайдено " + decoratedAds.size() + " оголошень"); // [cite: 188]
    }


    @FXML
    private void handleClearFilters() {
        if (minPriceField != null) minPriceField.clear(); // [cite: 189]
        if (maxPriceField != null) maxPriceField.clear(); // [cite: 190]
        if (statusFilterCombo != null) statusFilterCombo.setValue("Всі"); // [cite: 191]
        if (premiumOnlyCheckBox != null) premiumOnlyCheckBox.setSelected(false); // [cite: 192]
        if (urgentOnlyCheckBox != null) urgentOnlyCheckBox.setSelected(false); // [cite: 193]
        refreshCurrentView(); // [cite: 194]
        updateActiveFiltersDisplay(); // [cite: 195]
        updateStatus("Фільтри розширеного пошуку очищено"); // [cite: 196]
    }


    @FXML
    private void handleClearAllFilters() {
        handleClearFilters(); // [cite: 197]

        if (quickFilterPremium != null) quickFilterPremium.setSelected(false); // [cite: 198]
        if (quickFilterUrgent != null) quickFilterUrgent.setSelected(false); // [cite: 199]
        if (quickFilterWithDelivery != null) quickFilterWithDelivery.setSelected(false); // [cite: 200]
        if (quickFilterWithWarranty != null) quickFilterWithWarranty.setSelected(false); // [cite: 201]
        if (quickFilterWithDiscount != null) quickFilterWithDiscount.setSelected(false); // [cite: 202]
        refreshCurrentView(); // [cite: 203]
        updateActiveFiltersDisplay(); // [cite: 204]
        updateStatus("Всі фільтри очищено"); // [cite: 205]
    }


    @FXML
    private void handleToggleSortOrder() {
        isAscendingSort = !isAscendingSort; // [cite: 206]
        if (sortOrderButton != null) { // [cite: 207]
            sortOrderButton.setText(isAscendingSort ? "↑" : "↓"); // [cite: 207]
        }
        applySorting(); // [cite: 208]
        updateStatus("Порядок сортування змінено на " + (isAscendingSort ? "зростаючий" : "спадаючий")); // [cite: 209]
    }


    private void handleSortChange() {
        if (sortComboBox == null || sortComboBox.getValue() == null) return; // [cite: 210]
        String selectedSort = sortComboBox.getValue(); // [cite: 211]
        switch (selectedSort) { // [cite: 211]
            case "За назвою": // [cite: 212]
                currentSortBy = "title"; // [cite: 212]
                break; // [cite: 213]
            case "За ціною": // [cite: 214]
                currentSortBy = "price"; // [cite: 214]
                break; // [cite: 215]
            case "За датою": // [cite: 216]
                currentSortBy = "date"; // [cite: 216]
                break; // [cite: 217]
            case "За популярністю": // Assuming 'popularity' is a sortable field in Ad // [cite: 218]
                currentSortBy = "popularity"; // [cite: 218]
                break; // [cite: 219]
            default: // [cite: 220]
                currentSortBy = "title"; // [cite: 220]
                break; // [cite: 221]
        }
        applySorting(); // [cite: 222]
        updateStatus("Сортування змінено на: " + selectedSort); // [cite: 223]
    }


    private void applySorting() {
        if (adsObservableList != null && !adsObservableList.isEmpty()) { // [cite: 224]
            adsObservableList.sort((ac1, ac2) -> { // [cite: 224]
                if (ac1 == null && ac2 == null) return 0; // [cite: 224]
                if (ac1 == null || ac1.getAd() == null) return isAscendingSort ? 1 : -1; // [cite: 224]
                if (ac2 == null || ac2.getAd() == null) return isAscendingSort ? -1 : 1; // [cite: 224]

                Ad ad1 = ac1.getAd(); // [cite: 224]
                Ad ad2 = ac2.getAd(); // [cite: 225]

                int comparisonResult = 0; // [cite: 225]
                switch (currentSortBy) { // [cite: 225]
                    case "title": // [cite: 225]
                        if (ad1.getTitle() != null && ad2.getTitle() != null) { // [cite: 226]
                            comparisonResult = ad1.getTitle().compareToIgnoreCase(ad2.getTitle()); // [cite: 226]
                        } else if (ad1.getTitle() == null && ad2.getTitle() != null) { // [cite: 226]
                            comparisonResult = -1; // [cite: 227]
                        } else if (ad1.getTitle() != null && ad2.getTitle() == null) { // [cite: 228]
                            comparisonResult = 1; // [cite: 228]
                        }
                        break; // [cite: 229]
                    case "price": // [cite: 230]
                        comparisonResult = Double.compare(ad1.getPrice(), ad2.getPrice()); // [cite: 230]
                        break; // [cite: 231]
                    case "date": // [cite: 232]
                        if (ad1.getCreatedAt() != null && ad2.getCreatedAt() != null) { // [cite: 232]
                            comparisonResult = ad1.getCreatedAt().compareTo(ad2.getCreatedAt()); // [cite: 232]
                        } else if (ad1.getCreatedAt() == null && ad2.getCreatedAt() != null) { // [cite: 233]
                            comparisonResult = -1; // [cite: 233]
                        } else if (ad1.getCreatedAt() != null && ad2.getCreatedAt() == null) { // [cite: 234]
                            comparisonResult = 1; // [cite: 234]
                        }
                        break; // [cite: 235]
                    case "popularity": // [cite: 236]
                        // Assuming Ad has a getPopularity() method returning a comparable type (e.g., int or double)
                        // comparisonResult = Double.compare(ad1.getPopularity(), ad2.getPopularity()); // [cite: 236]
                        // System.out.println("Sorting by popularity not yet fully implemented for Ad object."); // [cite: 237]
                        break; // [cite: 238]
                    default: // [cite: 239]
                        break; // [cite: 239]
                }
                return isAscendingSort ? comparisonResult : -comparisonResult; // [cite: 240]
            });
        }
        if (adListView != null) adListView.refresh(); // [cite: 242]
        System.out.println("Applying sort by: " + currentSortBy + (isAscendingSort ? " ASC" : " DESC")); // [cite: 243]
    }


    @FXML
    private void handleSwitchToListView() {
        if (listViewButton != null) listViewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // [cite: 244]
        if (gridViewButton != null) gridViewButton.setStyle(""); // [cite: 245]
        updateStatus("Перемкнуто на вигляд списку"); // [cite: 246]
    }


    @FXML
    private void handleSwitchToGridView() {
        if (gridViewButton != null) gridViewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // [cite: 247]
        if (listViewButton != null) listViewButton.setStyle(""); // [cite: 248]
        updateStatus("Перемкнуто на вигляд сітки"); // [cite: 249]
    }


    @FXML
    private void handleRefresh() {
        showLoadingIndicator("Оновлення..."); // [cite: 250]
        refreshCurrentView(); // [cite: 251]
        updateLastUpdateTime(); // [cite: 252]
        hideLoadingIndicator(); // [cite: 253]
        updateStatus("Список оновлено"); // [cite: 254]
    }


    // ========== ПАГІНАЦІЯ ==========


    @FXML
    private void handleFirstPage() {
        if (currentPage > 1) { // [cite: 255]
            currentPage = 1; // [cite: 255]
            refreshCurrentView(); // [cite: 256]
            updateStatus("Перехід на першу сторінку"); // [cite: 257]
        }
    }


    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) { // [cite: 258]
            currentPage--; // [cite: 258]
            refreshCurrentView(); // [cite: 259]
            updateStatus("Перехід на попередню сторінку"); // [cite: 260]
        }
    }


    @FXML
    private void handleNextPage() {
        int totalPages = getTotalPages(); // [cite: 261]
        if (currentPage < totalPages) { // [cite: 262]
            currentPage++; // [cite: 262]
            refreshCurrentView(); // [cite: 263]
            updateStatus("Перехід на наступну сторінку"); // [cite: 264]
        }
    }


    @FXML
    private void handleLastPage() {
        int totalPages = getTotalPages(); // [cite: 265]
        if (currentPage < totalPages) { // [cite: 266]
            currentPage = totalPages; // [cite: 266]
            refreshCurrentView(); // [cite: 267]
            updateStatus("Перехід на останню сторінку"); // [cite: 268]
        }
    }


    private void handlePageSizeChange() {
        if (pageSizeComboBox == null || pageSizeComboBox.getValue() == null) return; // [cite: 269]
        Integer newPageSize = pageSizeComboBox.getValue(); // [cite: 270]
        if (newPageSize != null && newPageSize > 0) { // [cite: 271]
            pageSize = newPageSize; // [cite: 271]
            currentPage = 1; // [cite: 272]
            refreshCurrentView(); // [cite: 273]
            updateStatus("Розмір сторінки змінено на " + pageSize); // [cite: 274]
        }
    }


    private void updatePaginationControls() {
        int totalItems = adsObservableList.size(); // [cite: 275]
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize)); // [cite: 276]
        if (currentPage > totalPages) { // [cite: 277]
            currentPage = totalPages; // [cite: 277]
        }
        if (currentPage < 1) { // [cite: 278]
            currentPage = 1; // [cite: 278]
        }

        if (pageInfoLabel != null) { // [cite: 279]
            pageInfoLabel.setText("Сторінка " + currentPage + " з " + totalPages); // [cite: 279]
        }

        if (firstPageButton != null) firstPageButton.setDisable(currentPage <= 1); // [cite: 280]
        if (prevPageButton != null) prevPageButton.setDisable(currentPage <= 1); // [cite: 281]
        if (nextPageButton != null) nextPageButton.setDisable(currentPage >= totalPages); // [cite: 282]
        if (lastPageButton != null) lastPageButton.setDisable(currentPage >= totalPages); // [cite: 283]

        if (paginationControls != null) { // [cite: 284]
            paginationControls.setVisible(totalPages > 1); // [cite: 284]
            paginationControls.setManaged(totalPages > 1); // [cite: 285]
        }

        if (adListView != null) { // [cite: 286]
            int fromIndex = (currentPage - 1) * pageSize; // [cite: 286]
            int toIndex = Math.min(fromIndex + pageSize, totalItems); // [cite: 287]

            if (fromIndex < totalItems) { // [cite: 288]
                List<AdComponent> pageData = adsObservableList.subList(fromIndex, toIndex); // [cite: 288]
                adListView.setItems(FXCollections.observableArrayList(pageData)); // [cite: 289]
            } else {
                adListView.setItems(FXCollections.emptyObservableList()); // [cite: 290]
            }
        }
    }

    private void updatePagination() {
        updatePaginationControls(); // [cite: 291]
    }



    private int getTotalPages() {
        if (adsObservableList == null || pageSize <= 0) return 1; // [cite: 292]
        int totalAds = adsObservableList.size(); // [cite: 293]
        return Math.max(1, (int) Math.ceil((double) totalAds / pageSize)); // [cite: 294]
    }


    // ========== ДОПОМІЖНІ МЕТОДИ ==========


    private void updateActiveFiltersDisplay() {
        if (activeFiltersContainer == null || activeFiltersPanel == null) return; // [cite: 295]
        activeFiltersContainer.getChildren().clear(); // [cite: 296]
        boolean hasActiveFilters = false; // [cite: 297]

        if (minPriceField != null && !minPriceField.getText().isEmpty()) { // [cite: 298]
            addFilterChip("Мін. ціна: " + minPriceField.getText()); // [cite: 298]
            hasActiveFilters = true; // [cite: 299]
        }
        if (maxPriceField != null && !maxPriceField.getText().isEmpty()) { // [cite: 300]
            addFilterChip("Макс. ціна: " + maxPriceField.getText()); // [cite: 300]
            hasActiveFilters = true; // [cite: 301]
        }
        if (statusFilterCombo != null && statusFilterCombo.getValue() != null && !"Всі".equals(statusFilterCombo.getValue())) { // [cite: 302]
            addFilterChip("Статус: " + statusFilterCombo.getValue()); // [cite: 302]
            hasActiveFilters = true; // [cite: 303]
        }
        if (premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected()) { // [cite: 304]
            addFilterChip("Тільки преміум (розш.)"); // [cite: 304]
            hasActiveFilters = true; // [cite: 305]
        }
        if (urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected()) { // [cite: 306]
            addFilterChip("Тільки терміново (розш.)"); // [cite: 306]
            hasActiveFilters = true; // [cite: 307]
        }

        if (quickFilterPremium != null && quickFilterPremium.isSelected()) { // [cite: 308]
            addFilterChip("⭐ Преміум"); // [cite: 308]
            hasActiveFilters = true; // [cite: 309]
        }
        if (quickFilterUrgent != null && quickFilterUrgent.isSelected()) { // [cite: 310]
            addFilterChip("🚨 Терміново"); // [cite: 310]
            hasActiveFilters = true; // [cite: 311]
        }
        if (quickFilterWithDelivery != null && quickFilterWithDelivery.isSelected()) { // [cite: 312]
            addFilterChip("🚚 З доставкою"); // [cite: 312]
            hasActiveFilters = true; // [cite: 313]
        }
        if (quickFilterWithWarranty != null && quickFilterWithWarranty.isSelected()) { // [cite: 314]
            addFilterChip("🛡️ З гарантією"); // [cite: 314]
            hasActiveFilters = true; // [cite: 315]
        }
        if (quickFilterWithDiscount != null && quickFilterWithDiscount.isSelected()) { // [cite: 316]
            addFilterChip("💰 Зі знижкою"); // [cite: 316]
            hasActiveFilters = true; // [cite: 317]
        }

        activeFiltersPanel.setVisible(hasActiveFilters); // [cite: 318]
        activeFiltersPanel.setManaged(hasActiveFilters); // [cite: 319]
    }


    private void addFilterChip(String text) {
        if (activeFiltersContainer != null) { // [cite: 320]
            Label filterChip = new Label(text); // [cite: 320]
            filterChip.getStyleClass().add("filter-chip"); // [cite: 321]
            HBox.setMargin(filterChip, new Insets(0, 5, 0, 0)); // [cite: 322]
            activeFiltersContainer.getChildren().add(filterChip); // [cite: 323]
        }
    }


    private void updateStatistics() {
        if (totalAdsLabel != null && adService != null) { // [cite: 324]
            if (adsObservableList != null) { // [cite: 324]
                totalAdsLabel.setText("Всього (фільтр.): " + adsObservableList.size()); // [cite: 324]
            }
        }

        if (filteredAdsLabel != null && adListView != null && adListView.getItems() != null) { // [cite: 325]
            filteredAdsLabel.setText("На сторінці: " + adListView.getItems().size()); // [cite: 325]
        }
        if (selectedCategoryLabel != null && currentSelectedCategoryId == null) { // [cite: 326]
            selectedCategoryLabel.setText("Обрана категорія: Всі"); // [cite: 326]
        }
    }


    private void updateMediatorStatus(String status) {
        if (mediatorStatusLabel != null) { // [cite: 327]
            mediatorStatusLabel.setText("Медіатор: " + status); // [cite: 327]
        }
    }


    private void updateLastUpdateTime() {
        if (lastUpdateLabel != null) { // [cite: 328]
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")); // [cite: 328]
            lastUpdateLabel.setText("Останнє оновлення: " + currentTime); // [cite: 329]
        }
    }


    private void updateStatus(String message) {
        if (statusLabel != null) { // [cite: 330]
            statusLabel.setText(message); // [cite: 330]
        }
    }


    private void showLoadingIndicator(String message) {
        if (loadingIndicator != null) { // [cite: 331]
            loadingIndicator.setVisible(true); // [cite: 331]
            loadingIndicator.setManaged(true); // [cite: 332]
        }
        if (loadingLabel != null) { // [cite: 333]
            loadingLabel.setText(message); // [cite: 333]
        }
    }


    private void hideLoadingIndicator() {
        if (loadingIndicator != null) { // [cite: 334]
            loadingIndicator.setVisible(false); // [cite: 334]
            loadingIndicator.setManaged(false); // [cite: 335]
        }
        if (loadingLabel != null) { // [cite: 336]
            loadingLabel.setText(""); // [cite: 336]
        }
    }


    // ========== ІСНУЮЧІ МЕТОДИ (БЕЗ ЗМІН, АЛЕ ПЕРЕВІРЕНІ) ==========


    private void initializeCommandManager() {
        if (adService == null) { // [cite: 337]
            showErrorAlert("Критична помилка", "AdService не ініціалізовано.", "Неможливо створити CommandManager."); // [cite: 337]
            return; // [cite: 338]
        }
        CommandInvoker commandInvoker = new CommandInvoker(); // [cite: 339]
        CommandFactory commandFactory = new CommandFactory(adService); // [cite: 340]
        commandManager = new AdCommandManager(commandInvoker, commandFactory); // [cite: 341]
    }


    private void setupCommandHistoryView() {
        if (commandHistoryListView != null) { // [cite: 342]
            commandHistoryListView.setItems(commandHistoryObservableList); // [cite: 342]
            commandHistoryListView.setPrefHeight(150); // [cite: 343]
        }
    }


    private void updateCommandButtons() {
        if (commandManager == null) return; // [cite: 344]
        if (undoButton != null) { // [cite: 345]
            undoButton.setDisable(!commandManager.canUndo()); // [cite: 345]
        }
        if (redoButton != null) { // [cite: 346]
            redoButton.setDisable(!commandManager.canRedo()); // [cite: 346]
        }
        if (commandHistoryObservableList != null) { // [cite: 347]
            commandHistoryObservableList.setAll(commandManager.getCommandHistory()); // [cite: 347]
        }
    }


    private void setupCategoryTree() {
        if (categoryTreeView == null || categoryService == null) { // [cite: 348]
            System.err.println("Error: categoryTreeView or categoryService is null. Cannot setup category tree."); // [cite: 348]
            if (categoryTreeView != null) { // [cite: 349]
                TreeItem<CategoryComponent> fallbackRoot = new TreeItem<>(new Category("fallback", "Помилка завантаження категорій", null)); // [cite: 349]
                categoryTreeView.setRoot(fallbackRoot); // [cite: 350]
            }
            return; // [cite: 351]
        }

        try {
            List<CategoryComponent> rootCategories = categoryService.getAllRootCategories(); // [cite: 352]
            if (rootCategories == null) { // [cite: 353]
                rootCategories = new ArrayList<>(); // [cite: 353]
                System.err.println("Warning: CategoryService.getAllRootCategories() returned null. Using empty list."); // [cite: 354]
            }

            Category allCategoriesRootNode = new Category("root", "Всі категорії", null); // [cite: 355]
            TreeItem<CategoryComponent> rootTreeItem = new TreeItem<>(allCategoriesRootNode); // [cite: 356]
            rootTreeItem.setExpanded(true); // [cite: 357]

            for (CategoryComponent rootCategory : rootCategories) { // [cite: 358]
                if (rootCategory != null) { // [cite: 358]
                    TreeItem<CategoryComponent> categoryItem = createTreeItem(rootCategory, true); // [cite: 358]
                    if (categoryItem != null) { // [cite: 359]
                        rootTreeItem.getChildren().add(categoryItem); // [cite: 359]
                    }
                } else {
                    System.err.println("Warning: Found null root category, skipping..."); // [cite: 360]
                }
            }

            categoryTreeView.setRoot(rootTreeItem); // [cite: 361]
            categoryTreeView.setShowRoot(true); // [cite: 362]

            categoryTreeView.setCellFactory(tv -> new TreeCell<CategoryComponent>() { // [cite: 363]
                @Override
                protected void updateItem(CategoryComponent item, boolean empty) {
                    super.updateItem(item, empty); // [cite: 363]
                    if (empty || item == null) { // [cite: 363]
                        setText(null); // [cite: 363]
                    } else {
                        String name = item.getName(); // [cite: 364]
                        setText(name != null ? name : "Невідома категорія"); // [cite: 364]
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error setting up category tree: " + e.getMessage()); // [cite: 366]
            e.printStackTrace(); // [cite: 367]
            Category fallbackRootData = new Category("error_root", "Помилка завантаження", null); // [cite: 368]
            TreeItem<CategoryComponent> fallbackItem = new TreeItem<>(fallbackRootData); // [cite: 369]
            fallbackItem.setExpanded(true); // [cite: 370]
            categoryTreeView.setRoot(fallbackItem); // [cite: 371]
            categoryTreeView.setShowRoot(true); // [cite: 372]
        }
    }

    private TreeItem<CategoryComponent> createTreeItem(CategoryComponent categoryComponent, boolean autoExpand) {
        if (categoryComponent == null) { // [cite: 373]
            System.err.println("Warning: Attempting to create TreeItem for null categoryComponent"); // [cite: 373]
            return null; // [cite: 374]
        }

        TreeItem<CategoryComponent> item = new TreeItem<>(categoryComponent); // [cite: 375]
        item.setExpanded(autoExpand); // [cite: 376]
        if (categoryComponent instanceof Category) { // [cite: 377]
            Category category = (Category) categoryComponent; // [cite: 377]
            CategoryComponent[] childrenArray = category.getChildren(); // [cite: 378]
            if (childrenArray != null) { // [cite: 378]
                List<CategoryComponent> children = List.of(childrenArray); // [cite: 379]
                if (!children.isEmpty()) { // [cite: 380]
                    for (CategoryComponent child : children) { // [cite: 380]
                        if (child != null) { // [cite: 380]
                            TreeItem<CategoryComponent> childItem = createTreeItem(child, false); // [cite: 381]
                            if (childItem != null) { // [cite: 382]
                                item.getChildren().add(childItem); // [cite: 382]
                            }
                        } else {
                            System.err.println("Warning: Found null child category in category: " + (category.getName() != null ? category.getName() : "ID: " + category.getId())); // [cite: 383]
                        }
                    }
                }
            }
        }
        return item; // [cite: 384]
    }

    /*
    // Commented out original setupAdListView - the new one is above and uses AdComponent
    private void setupAdListView() {
        if (adListView != null) {
            adListView.setItems(adsObservableList); // [cite: 385]
            adListView.setCellFactory(new Callback<ListView<AdComponent>, ListCell<AdComponent>>() { // [cite: 386]
                @Override
                public ListCell<AdComponent> call(ListView<AdComponent> param) {
                    return new ListCell<AdComponent>() { // [cite: 386]
                        @Override
                        protected void updateItem(AdComponent item, boolean empty) { // [cite: 387]
                            super.updateItem(item, empty);
                            if (empty || item == null) { // [cite: 387]
                                setText(null); // [cite: 388]
                                setGraphic(null);
                            } else {
                                setText(item.getDisplayInfo()); // [cite: 388]
                                setOnMouseClicked(event -> {
                                    if (event.getClickCount() == 2) { // [cite: 389]
                                        handleOpenAdDetails(item.getAd()); // [cite: 390]
                                    }
                                });
                            }
                        }
                    };
                } // [cite: 392]
            });
        } // [cite: 393]
    }
    */

    private void validateCategoryData() {
        if (categoryService == null) return; // [cite: 394]
        try {
            List<CategoryComponent> rootCategories = categoryService.getAllRootCategories(); // [cite: 395]
            if (rootCategories == null) { // [cite: 396]
                System.err.println("VALIDATION ERROR: getAllRootCategories() returned null"); // [cite: 396]
                return; // [cite: 397]
            }
            System.out.println("Validating " + rootCategories.size() + " root categories..."); // [cite: 398]
            for (int i = 0; i < rootCategories.size(); i++) { // [cite: 399]
                CategoryComponent category = rootCategories.get(i); // [cite: 399]
                if (category == null) { // [cite: 400]
                    System.err.println("VALIDATION ERROR: Root category at index " + i + " is null"); // [cite: 400]
                    continue; // [cite: 401]
                }
                validateCategory(category, "Root[" + i + "]"); // [cite: 402]
            }
            System.out.println("Category validation completed."); // [cite: 403]
        } catch (Exception e) {
            System.err.println("Error during category validation: " + e.getMessage()); // [cite: 404]
            e.printStackTrace(); // [cite: 405]
        }
    }

    private void validateCategory(CategoryComponent category, String path) {
        if (category == null) { // [cite: 406]
            System.err.println("VALIDATION ERROR: Category is null at path: " + path); // [cite: 406]
            return; // [cite: 407]
        }
        String name = category.getName(); // [cite: 408]
        String id = category.getId(); // [cite: 409]
        if (name == null) { // [cite: 410]
            System.err.println("VALIDATION WARNING: Category name is null at path: " + path + " (ID: " + id + ")"); // [cite: 410]
        }
        if (id == null) { // [cite: 411]
            System.err.println("VALIDATION WARNING: Category id is null at path: " + path + " (Name: " + name + ")"); // [cite: 411]
        }

        if (category instanceof Category) { // [cite: 412]
            Category cat = (Category) category; // [cite: 412]
            CategoryComponent[] childrenArray = cat.getChildren(); // [cite: 413]
            if (childrenArray != null) { // [cite: 413]
                List<CategoryComponent> children = List.of(childrenArray); // [cite: 414]
                for (int i = 0; i < children.size(); i++) { // [cite: 415]
                    CategoryComponent child = children.get(i); // [cite: 415]
                    validateCategory(child, path + " -> " + (name != null ? name : "null_name") + "[" + i + "]"); // [cite: 416]
                }
            }
        }
    }


    private void loadAds(String categoryId) {
        if (adService == null) { // [cite: 417]
            showErrorAlert("Помилка", "Сервіс оголошень недоступний.", "Неможливо завантажити оголошення."); // [cite: 417]
            return; // [cite: 418]
        }
        showLoadingIndicator("Завантаження оголошень..."); // [cite: 419]
        List<Ad> ads; // [cite: 420]
        String keyword = searchField != null ? searchField.getText() : ""; // [cite: 421]
        ads = adService.searchAds(keyword, null, null, categoryId); // [cite: 422]

        List<Ad> filteredByQuickFilters = new ArrayList<>(); // [cite: 423]
        if (ads != null) { // [cite: 424]
            for (Ad ad : ads) { // [cite: 424]
                boolean pass = true; // [cite: 424]
                if (quickFilterPremium != null && quickFilterPremium.isSelected() && !ad.isPremium()) pass = false; // [cite: 425]
                if (quickFilterUrgent != null && quickFilterUrgent.isSelected() && !ad.isUrgent()) pass = false; // [cite: 426]
                if (quickFilterWithDelivery != null && quickFilterWithDelivery.isSelected() && !ad.hasDelivery()) pass = false; // [cite: 427]
                if (quickFilterWithWarranty != null && quickFilterWithWarranty.isSelected() && !ad.hasWarranty()) pass = false; // [cite: 428]
                if (quickFilterWithDiscount != null && quickFilterWithDiscount.isSelected() && !ad.hasDiscount()) pass = false; // [cite: 429]

                if (pass) { // [cite: 430]
                    filteredByQuickFilters.add(ad); // [cite: 430]
                }
            }
        }


        List<AdComponent> decoratedAds = filteredByQuickFilters.stream() // [cite: 431]
                .map(this::createDecoratedAd) // [cite: 431]
                .toList(); // [cite: 432]
        adsObservableList.setAll(decoratedAds); // [cite: 432]

        applySorting(); // [cite: 433]
        updatePaginationControls(); // [cite: 434]
        updateStatistics(); // [cite: 435]
        hideLoadingIndicator(); // [cite: 436]
        updateStatus("Завантажено " + adsObservableList.size() + " відповідних оголошень (до пагінації). На сторінці: " + (adListView.getItems() != null ? adListView.getItems().size() : 0) ); // [cite: 437]
    }



    private void refreshCurrentView() {
        loadAds(currentSelectedCategoryId); // [cite: 438]
    }

    private void handleOpenAdDetails(Ad ad) {
        if (ad == null) return; // [cite: 439]
        try {
            MainGuiApp.loadAdDetailScene(ad); // [cite: 440]
        } catch (IOException e) {
            e.printStackTrace(); // [cite: 441]
            showErrorAlert("Помилка", "Не вдалося відкрити деталі оголошення", e.getMessage()); // [cite: 442]
        }
    }

    @FXML
    private void handleSearchAds() {
        // This method might serve as a fallback or an alternative trigger.
        // refreshCurrentView() handles loading ads based on the current searchField text and other filters.
        refreshCurrentView(); // [cite: 444]
        if (searchField != null) { // [cite: 445]
            updateStatus("Пошук за запитом: " + searchField.getText()); // [cite: 445]
        }
        // Removed redundant call to searchComponent.performSearch() here as refreshCurrentView already handles the search criteria. // [cite: 446]
    }


    @FXML
    private void handleCreateAd() {
        try {
            MainGuiApp.loadCreateAdScene(); // [cite: 447]
            Platform.runLater(() -> { // [cite: 448]
                refreshCurrentView(); // [cite: 448]
                updateStatus("Список оновлено після можливого створення оголошення."); // [cite: 448]
            });
        } catch (IOException e) {
            e.printStackTrace(); // [cite: 449]
            showErrorAlert("Помилка", "Не вдалося відкрити форму створення оголошення", e.getMessage()); // [cite: 450]
        }
    }

    @FXML
    private void handleLogout() {
        GlobalContext.getInstance().setLoggedInUser(null); // [cite: 451]
        try {
            MainGuiApp.loadLoginScene(); // [cite: 452]
        } catch (IOException e) {
            e.printStackTrace(); // [cite: 453]
            showErrorAlert("Помилка виходу", "Не вдалося завантажити сторінку входу.", e.getMessage()); // [cite: 454]
            Platform.exit(); // [cite: 455]
        }
    }

    @FXML
    private void handleExitApplication() {
        Optional<ButtonType> result = showConfirmationAlert( // [cite: 456]
                "Підтвердження виходу", // [cite: 456]
                "Ви впевнені, що хочете закрити програму?", // [cite: 456]
                "Всі незбережені зміни можуть бути втрачені." // [cite: 456]
        );
        if (result.isPresent() && result.get() == ButtonType.OK) { // [cite: 457]
            Platform.exit(); // [cite: 458]
        }
    }

    // ========== COMMAND PATTERN HANDLERS ==========

    @FXML
    private void handleUndo() {
        if (commandManager == null) return; // [cite: 459]
        if (commandManager.canUndo()) { // [cite: 460]
            try {
                commandManager.undo(); // [cite: 460]
                refreshCurrentView(); // [cite: 461]
                updateCommandButtons(); // [cite: 462]
                updateStatus("Команда скасована"); // [cite: 463]
            } catch (UserNotFoundException e) {
                showErrorAlert("Помилка скасування", "Не вдалося скасувати команду.", e.getMessage()); // [cite: 464]
            }
        }
    }

    @FXML
    private void handleRedo() {
        if (commandManager == null) return; // [cite: 465]
        if (commandManager.canRedo()) { // [cite: 466]
            try {
                commandManager.redo(); // [cite: 466]
                refreshCurrentView(); // [cite: 467]
                updateCommandButtons(); // [cite: 468]
                updateStatus("Команда повторена"); // [cite: 469]
            } catch (UserNotFoundException e) {
                showErrorAlert("Помилка повторення", "Не вдалося повторити команду.", e.getMessage()); // [cite: 470]
            }
        }
    }

    @FXML
    private void handleClearHistory() {
        if (commandManager == null) return; // [cite: 471]
        Optional<ButtonType> result = showConfirmationAlert( // [cite: 472]
                "Очистити історію команд", // [cite: 472]
                "Ви впевнені, що хочете очистити історію команд?", // [cite: 472]
                "Цю дію неможливо скасувати." // [cite: 472]
        );
        if (result.isPresent() && result.get() == ButtonType.OK) { // [cite: 473]
            commandManager.clearHistory(); // [cite: 473]
            updateCommandButtons(); // [cite: 474]
            updateStatus("Історія команд очищена"); // [cite: 475]
        }
    }

    // ========== UTILITY METHODS ==========

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR); // [cite: 476]
        alert.setTitle("Помилка"); // [cite: 477]
        alert.setHeaderText(null); // [cite: 478]
        alert.setContentText(message); // [cite: 479]
        alert.showAndWait(); // [cite: 480]
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR); // [cite: 481]
        alert.setTitle(title); // [cite: 482]
        alert.setHeaderText(header); // [cite: 483]
        alert.setContentText(content); // [cite: 484]
        alert.showAndWait(); // [cite: 485]
    }

    private Optional<ButtonType> showConfirmationAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // [cite: 486]
        alert.setTitle(title); // [cite: 487]
        alert.setHeaderText(header); // [cite: 488]
        alert.setContentText(content); // [cite: 489]
        return alert.showAndWait(); // [cite: 490]
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // [cite: 491]
        alert.setTitle(title); // [cite: 492]
        alert.setHeaderText(header); // [cite: 493]
        alert.setContentText(content); // [cite: 494]
        alert.showAndWait(); // [cite: 495]
    }

    // MODIFIED METHOD createDecoratedAd
    private AdComponent createDecoratedAd(Ad ad) {
        if (ad == null) return null; // [cite: 496]
        AdDecoratorFactory decoratorFactory = new AdDecoratorFactory(); // [cite: 497]

        // Correction for "cannot find symbol method createBaseAdComponent(com.example.olx.domain.model.Ad)"
        // Assumes Ad object can be directly used as an AdComponent (e.g., Ad implements AdComponent)
        AdComponent baseComponent = (AdComponent) ad; // [cite: 498] (Modified line)

        if (ad.isPremium()) { // [cite: 498]
            // Assuming createPremiumAd takes AdComponent - NO ERROR REPORTED FOR THIS LINE
            baseComponent = decoratorFactory.createPremiumAd((Ad) baseComponent); // [cite: 499]
        }
        if (ad.isUrgent()) { // [cite: 499]
            // Assuming createUrgentAd takes AdComponent - NO ERROR REPORTED FOR THIS LINE
            baseComponent = decoratorFactory.createUrgentAd((Ad) baseComponent); // [cite: 500]
        }
        if (ad.hasDiscount()) { // [cite: 500]
            // Correction for "method createDiscountAd in class ...AdDecoratorFactory cannot be applied to given types"
            // Assumes createDiscountAd takes the original Ad object and returns an AdComponent,
            // due to inconsistency with createPremiumAd/createUrgentAd if they accept AdComponent.
            baseComponent = decoratorFactory.createDiscountAd(ad); // [cite: 501] (Modified line)
        }
        // if (ad.hasDelivery()) { baseComponent = decoratorFactory.createDeliveryAd(baseComponent); } // [cite: 502]
        // if (ad.hasWarranty()) { baseComponent = decoratorFactory.createWarrantyAd(baseComponent); } // [cite: 503]

        return baseComponent; // [cite: 504]
    }

    // ========== MEDIATOR INTEGRATION METHODS ==========

    public void updateAdsList(List<Ad> adsFromMediator) {
        if (adsFromMediator == null) adsFromMediator = new ArrayList<>(); // [cite: 505]
        List<AdComponent> decoratedAds = adsFromMediator.stream() // [cite: 506]
                .map(this::createDecoratedAd) // [cite: 506]
                .filter(java.util.Objects::nonNull) // [cite: 506]
                .toList(); // [cite: 506]
        Platform.runLater(() -> { // [cite: 507]
            adsObservableList.setAll(decoratedAds); // [cite: 507]
            applySorting(); // [cite: 507]
            updatePaginationControls(); // [cite: 507]
            updateStatistics(); // [cite: 507]
            updateStatus("Список оновлено через медіатор. " + decoratedAds.size() + " оголошень."); // [cite: 507]
            hideLoadingIndicator(); // [cite: 507]
        });
    }

    public void updateMediatorMessage(String message) {
        Platform.runLater(() -> { // [cite: 509]
            updateStatus(message); // [cite: 509]
            updateMediatorStatus("активний (повідомлення)"); // [cite: 509]
        });
    }

    public User getCurrentUser() {
        return GlobalContext.getInstance().getLoggedInUser(); // [cite: 510]
    }

    public void logAction(String action) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")); // [cite: 511]
        String logMessage = "[" + timestamp + "] " + action; // [cite: 512]
        Platform.runLater(() -> { // [cite: 513]
            if (commandHistoryObservableList != null) { // [cite: 513]
                if (commandHistoryObservableList.size() > 100) { // [cite: 513]
                    commandHistoryObservableList.remove(0); // [cite: 513]
                }
                commandHistoryObservableList.add(logMessage); // [cite: 513]
                if (commandHistoryListView != null && !commandHistoryObservableList.isEmpty()) { // [cite: 514]
                    commandHistoryListView.scrollTo(commandHistoryObservableList.size() - 1); // [cite: 514]
                }
            }
        });
    }

    // ========== CLEANUP ==========

    public void cleanup() {
        updateStatus("Очищення контролера..."); // [cite: 515]
        if (mediator != null) { // [cite: 516]
            updateMediatorStatus("неактивний (очищено)"); // [cite: 516]
        }

        System.out.println("MainController cleanup finished."); // [cite: 517]
    }
}
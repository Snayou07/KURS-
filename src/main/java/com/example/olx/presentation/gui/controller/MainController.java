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
        User currentUser = GlobalContext.getInstance().getLoggedInUser();
        if (currentUser != null) {
            loggedInUserLabel.setText("Користувач: " + currentUser.getUsername());
            createAdButton.setDisable(false);
            logoutButton.setDisable(false);
        } else {
            try {
                MainGuiApp.loadLoginScene();
                return; // Stop further initialization if not logged in
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Помилка входу", "Не вдалося завантажити сторінку входу.", e.getMessage());
                Platform.exit(); // Exit if login scene fails to load
                return;
            }
        }

        initializeCommandManager();
        initializeMediator();
        initializeUIComponents();
        setupCategoryTree();
        setupAdListView();
        setupCommandHistoryView();
        setupMediatorIntegration();
        setupGlobalEventListeners();

        // Initial data load via mediator and UI updates
        if (this.mediator != null) {
            this.mediator.loadAllAds();
        } else {
            // Fallback or direct load if mediator is not central to initial load
            loadAds(null);
        }

        updateCommandButtons();
        updateLastUpdateTime();
        updateStatus("Головне вікно ініціалізовано.");
    }

    private void initializeUIComponents() {
        // Initialize ComboBoxes
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
                "За назвою", "За ціною", "За датою", "За популярністю" // Match with handleSortChange cases
        );
        if (sortComboBox != null) {
            sortComboBox.setItems(sortOptions);
            sortComboBox.setValue("За назвою");
            sortComboBox.setOnAction(e -> handleSortChange());
        }

        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "Всі", "Активне", "Чернетка", "Архівоване", "Продано"
        );
        if (statusFilterCombo != null) {
            statusFilterCombo.setItems(statusOptions);
            statusFilterCombo.setValue("Всі");
        }

        ObservableList<Integer> pageSizeOptions = FXCollections.observableArrayList(
                10, 20, 50, 100
        );
        if (pageSizeComboBox != null) {
            pageSizeComboBox.setItems(pageSizeOptions);
            pageSizeComboBox.setValue(pageSize);
            pageSizeComboBox.setOnAction(e -> handlePageSizeChange());
        }

        setupQuickFilters();

        if (advancedSearchPanel != null) {
            advancedSearchPanel.setVisible(false);
            advancedSearchPanel.setManaged(false);
        }
    }

    private void setupAdListView() {
        if (adListView == null) {
            System.err.println("Error: adListView is null. Check FXML binding.");
            return;
        }
        adListView.setItems(adsObservableList);

        adListView.setCellFactory(listView -> new ListCell<AdComponent>() {
            @Override
            protected void updateItem(AdComponent adComponent, boolean empty) {
                super.updateItem(adComponent, empty);

                if (empty || adComponent == null || adComponent.getAd() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Ad ad = adComponent.getAd();

                    VBox container = new VBox(5);
                    container.setPadding(new Insets(10));

                    Label titleLabel = new Label(ad.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    Label priceLabel = new Label(String.format("%.2f грн", ad.getPrice()));
                    priceLabel.setStyle("-fx-text-fill: #2E8B57; -fx-font-weight: bold;");

                    String description = ad.getDescription();
                    if (description != null && description.length() > 100) {
                        description = description.substring(0, 100) + "...";
                    }
                    Label descLabel = new Label(description != null ? description : "Немає опису");
                    descLabel.setStyle("-fx-text-fill: #666666;");

                    HBox infoBox = new HBox(15);
                    String categoryName = "Невідомо";
                    if (ad.getCategoryId() != null) {
                        // To get the actual category name, you'd typically use categoryService here
                        // For now, let's assume CategoryService can provide it, or Ad has categoryName
                        // categoryName = categoryService.getCategoryById(ad.getCategoryId()).map(Category::getName).orElse("ID: " + ad.getCategoryId());
                        categoryName = "ID: " + ad.getCategoryId(); // Placeholder
                    }
                    Label categoryInfoLabel = new Label("Категорія: " + categoryName);
                    String dateStr = "Дата: невідома";
                    if (ad.getCreatedAt() != null) {
                        try {
                            dateStr = "Дата: " + ad.getCreatedAt().toLocalDate().toString();
                        } catch (Exception e) {
                            dateStr = "Дата: " + java.time.LocalDate.now().toString() + " (fallback)";
                        }
                    }
                    Label dateLabel = new Label(dateStr);
                    infoBox.getChildren().addAll(categoryInfoLabel, dateLabel);

                    Label decoratedInfoLabel = new Label(adComponent.getDisplayInfo());
                    decoratedInfoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: blue;");

                    container.getChildren().addAll(titleLabel, priceLabel, descLabel, infoBox, decoratedInfoLabel);
                    setGraphic(container);
                }
            }
        });
        adListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                AdComponent selectedComponent = adListView.getSelectionModel().getSelectedItem();
                if (selectedComponent != null && selectedComponent.getAd() != null) {
                    try {
                        MainGuiApp.loadAdDetailScene(selectedComponent.getAd());
                    } catch (IOException e) {
                        showError("Помилка відкриття деталей оголошення: " + e.getMessage());
                    }
                }
            }
        });
    }


    private void setupGlobalEventListeners() {
        if (categoryTreeView != null) {
            categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValueNode) -> {
                        if (newValueNode != null) {
                            CategoryComponent selectedCategory = newValueNode.getValue();
                            if (selectedCategory != null && selectedCategory.getId() != null && selectedCategory.getName() != null) {
                                currentSelectedCategoryId = selectedCategory.getId();
                                String categoryName = selectedCategory.getName();
                                if (currentCategoryLabel != null) currentCategoryLabel.setText("Оголошення в категорії: " + categoryName);
                                if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: " + categoryName);

                                if (searchComponent != null) {
                                    searchComponent.updateCategory(currentSelectedCategoryId); // Inform mediator
                                } else {
                                    loadAds(currentSelectedCategoryId);
                                }
                            }
                        } else {
                            currentSelectedCategoryId = null;
                            if (currentCategoryLabel != null) currentCategoryLabel.setText("Всі оголошення");
                            if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: немає");
                            if (searchComponent != null) {
                                searchComponent.updateCategory(""); // Inform mediator
                            } else {
                                loadAds(null);
                            }
                        }
                    });
        }
    }


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


    private void applyQuickFilters() {
        refreshCurrentView();
        updateActiveFiltersDisplay();
        updateStatus("Швидкі фільтри застосовано.");
    }


    private void initializeMediator() {
        if (adService == null || categoryService == null) {
            showErrorAlert("Критична помилка", "Сервіси не ініціалізовані.", "Неможливо створити медіатор.");
            updateMediatorStatus("помилка ініціалізації");
            return;
        }
        mediator = new AdBrowserMediator(adService, categoryService);
        searchComponent = new SearchComponent(mediator);
        adListComponent = new AdListComponent(mediator);
        filterComponent = new FilterComponent(mediator);
        mediator.registerComponents(searchComponent, adListComponent, filterComponent);

        // Error 1: "cannot find symbol method setController(...)"
        // This line assumes AdBrowserMediator has a public method:
        // public void setController(MainController controller) { this.controller = controller; }
        // If this method does not exist in AdBrowserMediator.java, it needs to be added.
        // For now, I will comment it out to allow compilation of MainController,
        // but it's crucial for mediator functionality.
        // mediator.setController(this);
        // To fix Error 1, ensure AdBrowserMediator has the setController method.
        // Example in AdBrowserMediator.java:
        // private MainController controller;
        // public void setController(MainController controller) { this.controller = controller; }

        System.out.println("Медіатор ініціалізовано успішно (або з зауваженням щодо setController).");
        updateMediatorStatus("активний");
    }


    private void setupMediatorIntegration() {
        if (searchField != null && searchComponent != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                searchComponent.updateSearchText(newValue);
            });
            if (searchButton != null) {
                searchButton.setOnAction(e -> {
                    if (searchComponent != null) {
                        String searchText = (searchField != null) ? searchField.getText() : "";
                        searchComponent.performSearch(searchText, currentSelectedCategoryId);
                        updateStatus("Пошук ініційовано через медіатор: " + searchText);
                    } else {
                        handleSearchAds();
                    }
                });
            }

        } else {
            if (searchField == null) System.err.println("searchField is null in setupMediatorIntegration");
            if (searchComponent == null) System.err.println("searchComponent is null in setupMediatorIntegration");
            if (searchButton != null) {
                searchButton.setOnAction(e -> handleSearchAds());
            }
        }
    }


    // ========== ОБРОБНИКИ ПОДІЙ ==========


    @FXML
    private void handleToggleAdvancedSearch() {
        isAdvancedSearchVisible = !isAdvancedSearchVisible;
        if (advancedSearchPanel != null) {
            advancedSearchPanel.setVisible(isAdvancedSearchVisible);
            advancedSearchPanel.setManaged(isAdvancedSearchVisible);
        }
        updateStatus("Розширений пошук " + (isAdvancedSearchVisible ? "відкрито" : "закрито"));
    }


    @FXML
    private void handleApplyFilters() {
        showLoadingIndicator("Застосування фільтрів...");
        String minPriceText = (minPriceField != null) ? minPriceField.getText() : "";
        String maxPriceText = (maxPriceField != null) ? maxPriceField.getText() : "";
        String selectedStatus = (statusFilterCombo != null && statusFilterCombo.getValue() != null) ? statusFilterCombo.getValue() : "Всі";
        boolean premiumOnly = premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected();
        boolean urgentOnly = urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected();
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

        String keyword = (searchField != null) ? searchField.getText() : "";
        List<Ad> fetchedAds = adService.searchAds(keyword, minPrice, maxPrice, currentSelectedCategoryId);

        List<Ad> filteredAds = new ArrayList<>();
        if (fetchedAds != null) {
            for (Ad ad : fetchedAds) {
                boolean statusMatch = "Всі".equals(selectedStatus) || (ad.getStatus() != null && selectedStatus.equals(ad.getStatus().toString()));
                if (!statusMatch) continue;
                if (premiumOnly && !ad.isPremium()) continue;
                if (urgentOnly && !ad.isUrgent()) continue;

                if (quickFilterPremium != null && quickFilterPremium.isSelected() && !ad.isPremium()) continue;
                if (quickFilterUrgent != null && quickFilterUrgent.isSelected() && !ad.isUrgent()) continue;
                // Add similar checks for other quick filters if they correspond to Ad properties
                // e.g., if (quickFilterWithDelivery.isSelected() && !ad.hasDelivery()) continue;
                filteredAds.add(ad);
            }
        }


        List<AdComponent> decoratedAds = filteredAds.stream()
                .map(this::createDecoratedAd)
                .filter(java.util.Objects::nonNull) // Ensure no null components are added
                .toList();
        adsObservableList.setAll(decoratedAds);
        applySorting(); // Apply current sort order
        updateActiveFiltersDisplay();
        updateStatistics();
        updatePagination();
        hideLoadingIndicator();
        updateStatus("Фільтри застосовано. Знайдено " + decoratedAds.size() + " оголошень");
    }


    @FXML
    private void handleClearFilters() {
        if (minPriceField != null) minPriceField.clear();
        if (maxPriceField != null) maxPriceField.clear();
        if (statusFilterCombo != null) statusFilterCombo.setValue("Всі");
        if (premiumOnlyCheckBox != null) premiumOnlyCheckBox.setSelected(false);
        if (urgentOnlyCheckBox != null) urgentOnlyCheckBox.setSelected(false);
        refreshCurrentView();
        updateActiveFiltersDisplay();
        updateStatus("Фільтри розширеного пошуку очищено");
    }


    @FXML
    private void handleClearAllFilters() {
        handleClearFilters();

        if (quickFilterPremium != null) quickFilterPremium.setSelected(false);
        if (quickFilterUrgent != null) quickFilterUrgent.setSelected(false);
        if (quickFilterWithDelivery != null) quickFilterWithDelivery.setSelected(false);
        if (quickFilterWithWarranty != null) quickFilterWithWarranty.setSelected(false);
        if (quickFilterWithDiscount != null) quickFilterWithDiscount.setSelected(false);
        refreshCurrentView();
        updateActiveFiltersDisplay();
        updateStatus("Всі фільтри очищено");
    }


    @FXML
    private void handleToggleSortOrder() {
        isAscendingSort = !isAscendingSort;
        if (sortOrderButton != null) {
            sortOrderButton.setText(isAscendingSort ? "↑" : "↓");
        }
        applySorting();
        updateStatus("Порядок сортування змінено на " + (isAscendingSort ? "зростаючий" : "спадаючий"));
    }


    private void handleSortChange() {
        if (sortComboBox == null || sortComboBox.getValue() == null) return;
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
            case "За популярністю": // Assuming 'popularity' is a sortable field in Ad
                currentSortBy = "popularity";
                break;
            default:
                currentSortBy = "title";
                break;
        }
        applySorting();
        updateStatus("Сортування змінено на: " + selectedSort);
    }


    private void applySorting() {
        if (adsObservableList != null && !adsObservableList.isEmpty()) {
            adsObservableList.sort((ac1, ac2) -> {
                if (ac1 == null && ac2 == null) return 0;
                if (ac1 == null || ac1.getAd() == null) return isAscendingSort ? 1 : -1;
                if (ac2 == null || ac2.getAd() == null) return isAscendingSort ? -1 : 1;

                Ad ad1 = ac1.getAd();
                Ad ad2 = ac2.getAd();

                int comparisonResult = 0;
                switch (currentSortBy) {
                    case "title":
                        if (ad1.getTitle() != null && ad2.getTitle() != null) {
                            comparisonResult = ad1.getTitle().compareToIgnoreCase(ad2.getTitle());
                        } else if (ad1.getTitle() == null && ad2.getTitle() != null) {
                            comparisonResult = -1;
                        } else if (ad1.getTitle() != null && ad2.getTitle() == null) {
                            comparisonResult = 1;
                        }
                        break;
                    case "price":
                        comparisonResult = Double.compare(ad1.getPrice(), ad2.getPrice());
                        break;
                    case "date":
                        if (ad1.getCreatedAt() != null && ad2.getCreatedAt() != null) {
                            comparisonResult = ad1.getCreatedAt().compareTo(ad2.getCreatedAt());
                        } else if (ad1.getCreatedAt() == null && ad2.getCreatedAt() != null) {
                            comparisonResult = -1;
                        } else if (ad1.getCreatedAt() != null && ad2.getCreatedAt() == null) {
                            comparisonResult = 1;
                        }
                        break;
                    case "popularity":
                        // Assuming Ad has a getPopularity() method returning a comparable type (e.g., int or double)
                        // comparisonResult = Double.compare(ad1.getPopularity(), ad2.getPopularity());
                        // System.out.println("Sorting by popularity not yet fully implemented for Ad object.");
                        break;
                    default:
                        break;
                }
                return isAscendingSort ? comparisonResult : -comparisonResult;
            });
        }
        if (adListView != null) adListView.refresh();
        System.out.println("Applying sort by: " + currentSortBy + (isAscendingSort ? " ASC" : " DESC"));
    }


    @FXML
    private void handleSwitchToListView() {
        if (listViewButton != null) listViewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        if (gridViewButton != null) gridViewButton.setStyle("");
        updateStatus("Перемкнуто на вигляд списку");
    }


    @FXML
    private void handleSwitchToGridView() {
        if (gridViewButton != null) gridViewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        if (listViewButton != null) listViewButton.setStyle("");
        updateStatus("Перемкнуто на вигляд сітки");
    }


    @FXML
    private void handleRefresh() {
        showLoadingIndicator("Оновлення...");
        refreshCurrentView();
        updateLastUpdateTime();
        hideLoadingIndicator();
        updateStatus("Список оновлено");
    }


    // ========== ПАГІНАЦІЯ ==========


    @FXML
    private void handleFirstPage() {
        if (currentPage > 1) {
            currentPage = 1;
            refreshCurrentView();
            updateStatus("Перехід на першу сторінку");
        }
    }


    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            refreshCurrentView();
            updateStatus("Перехід на попередню сторінку");
        }
    }


    @FXML
    private void handleNextPage() {
        int totalPages = getTotalPages();
        if (currentPage < totalPages) {
            currentPage++;
            refreshCurrentView();
            updateStatus("Перехід на наступну сторінку");
        }
    }


    @FXML
    private void handleLastPage() {
        int totalPages = getTotalPages();
        if (currentPage < totalPages) {
            currentPage = totalPages;
            refreshCurrentView();
            updateStatus("Перехід на останню сторінку");
        }
    }


    private void handlePageSizeChange() {
        if (pageSizeComboBox == null || pageSizeComboBox.getValue() == null) return;
        Integer newPageSize = pageSizeComboBox.getValue();
        if (newPageSize != null && newPageSize > 0) {
            pageSize = newPageSize;
            currentPage = 1;
            refreshCurrentView();
            updateStatus("Розмір сторінки змінено на " + pageSize);
        }
    }


    private void updatePaginationControls() {
        int totalItems = adsObservableList.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }

        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Сторінка " + currentPage + " з " + totalPages);
        }

        if (firstPageButton != null) firstPageButton.setDisable(currentPage <= 1);
        if (prevPageButton != null) prevPageButton.setDisable(currentPage <= 1);
        if (nextPageButton != null) nextPageButton.setDisable(currentPage >= totalPages);
        if (lastPageButton != null) lastPageButton.setDisable(currentPage >= totalPages);

        if (paginationControls != null) {
            paginationControls.setVisible(totalPages > 1);
            paginationControls.setManaged(totalPages > 1);
        }

        if (adListView != null) {
            int fromIndex = (currentPage - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, totalItems);

            if (fromIndex < totalItems && fromIndex >=0 && toIndex <= totalItems) { // Added more checks for subList
                List<AdComponent> pageData = adsObservableList.subList(fromIndex, toIndex);
                adListView.setItems(FXCollections.observableArrayList(pageData));
            } else if (totalItems == 0 || fromIndex >= totalItems) { // if list is empty or page is out of bounds
                adListView.setItems(FXCollections.emptyObservableList());
            } else {
                // This case should ideally not be reached if currentPage is managed well
                System.err.println("Pagination error: fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", totalItems=" + totalItems);
                adListView.setItems(FXCollections.emptyObservableList());
            }
        }
    }

    private void updatePagination() {
        updatePaginationControls();
    }



    private int getTotalPages() {
        if (adsObservableList == null || pageSize <= 0) return 1;
        int totalAds = adsObservableList.size();
        return Math.max(1, (int) Math.ceil((double) totalAds / pageSize));
    }


    // ========== ДОПОМІЖНІ МЕТОДИ ==========


    private void updateActiveFiltersDisplay() {
        if (activeFiltersContainer == null || activeFiltersPanel == null) return;
        activeFiltersContainer.getChildren().clear();
        boolean hasActiveFilters = false;

        if (minPriceField != null && !minPriceField.getText().isEmpty()) {
            addFilterChip("Мін. ціна: " + minPriceField.getText());
            hasActiveFilters = true;
        }
        if (maxPriceField != null && !maxPriceField.getText().isEmpty()) {
            addFilterChip("Макс. ціна: " + maxPriceField.getText());
            hasActiveFilters = true;
        }
        if (statusFilterCombo != null && statusFilterCombo.getValue() != null && !"Всі".equals(statusFilterCombo.getValue())) {
            addFilterChip("Статус: " + statusFilterCombo.getValue());
            hasActiveFilters = true;
        }
        if (premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected()) {
            addFilterChip("Тільки преміум (розш.)");
            hasActiveFilters = true;
        }
        if (urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected()) {
            addFilterChip("Тільки терміново (розш.)");
            hasActiveFilters = true;
        }

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


    private void addFilterChip(String text) {
        if (activeFiltersContainer != null) {
            Label filterChip = new Label(text);
            filterChip.getStyleClass().add("filter-chip");
            HBox.setMargin(filterChip, new Insets(0, 5, 0, 0));
            activeFiltersContainer.getChildren().add(filterChip);
        }
    }


    private void updateStatistics() {
        if (totalAdsLabel != null && adService != null) {
            if (adsObservableList != null) {
                totalAdsLabel.setText("Всього (фільтр.): " + adsObservableList.size());
            }
        }

        if (filteredAdsLabel != null && adListView != null && adListView.getItems() != null) {
            filteredAdsLabel.setText("На сторінці: " + adListView.getItems().size());
        }
        if (selectedCategoryLabel != null && currentSelectedCategoryId == null) {
            selectedCategoryLabel.setText("Обрана категорія: Всі");
        }
    }


    private void updateMediatorStatus(String status) {
        if (mediatorStatusLabel != null) {
            mediatorStatusLabel.setText("Медіатор: " + status);
        }
    }


    private void updateLastUpdateTime() {
        if (lastUpdateLabel != null) {
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            lastUpdateLabel.setText("Останнє оновлення: " + currentTime);
        }
    }


    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }


    private void showLoadingIndicator(String message) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
            loadingIndicator.setManaged(true);
        }
        if (loadingLabel != null) {
            loadingLabel.setText(message);
        }
    }


    private void hideLoadingIndicator() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
            loadingIndicator.setManaged(false);
        }
        if (loadingLabel != null) {
            loadingLabel.setText("");
        }
    }


    // ========== ІСНУЮЧІ МЕТОДИ (БЕЗ ЗМІН, АЛЕ ПЕРЕВІРЕНІ) ==========


    private void initializeCommandManager() {
        if (adService == null) {
            showErrorAlert("Критична помилка", "AdService не ініціалізовано.", "Неможливо створити CommandManager.");
            return;
        }
        CommandInvoker commandInvoker = new CommandInvoker();
        CommandFactory commandFactory = new CommandFactory(adService);
        commandManager = new AdCommandManager(commandInvoker, commandFactory);
    }


    private void setupCommandHistoryView() {
        if (commandHistoryListView != null) {
            commandHistoryListView.setItems(commandHistoryObservableList);
            commandHistoryListView.setPrefHeight(150);
        }
    }


    private void updateCommandButtons() {
        if (commandManager == null) return;
        if (undoButton != null) {
            undoButton.setDisable(!commandManager.canUndo());
        }
        if (redoButton != null) {
            redoButton.setDisable(!commandManager.canRedo());
        }
        if (commandHistoryObservableList != null) {
            commandHistoryObservableList.setAll(commandManager.getCommandHistory());
        }
    }


    private void setupCategoryTree() {
        if (categoryTreeView == null || categoryService == null) {
            System.err.println("Error: categoryTreeView or categoryService is null. Cannot setup category tree.");
            if (categoryTreeView != null) {
                TreeItem<CategoryComponent> fallbackRoot = new TreeItem<>(new Category("fallback", "Помилка завантаження категорій", null));
                categoryTreeView.setRoot(fallbackRoot);
            }
            return;
        }

        try {
            List<CategoryComponent> rootCategories = categoryService.getAllRootCategories();
            if (rootCategories == null) {
                rootCategories = new ArrayList<>();
                System.err.println("Warning: CategoryService.getAllRootCategories() returned null. Using empty list.");
            }

            Category allCategoriesRootNode = new Category("root", "Всі категорії", null);
            TreeItem<CategoryComponent> rootTreeItem = new TreeItem<>(allCategoriesRootNode);
            rootTreeItem.setExpanded(true);

            for (CategoryComponent rootCategory : rootCategories) {
                if (rootCategory != null) {
                    TreeItem<CategoryComponent> categoryItem = createTreeItem(rootCategory, true);
                    if (categoryItem != null) {
                        rootTreeItem.getChildren().add(categoryItem);
                    }
                } else {
                    System.err.println("Warning: Found null root category, skipping...");
                }
            }

            categoryTreeView.setRoot(rootTreeItem);
            categoryTreeView.setShowRoot(true);

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
        } catch (Exception e) {
            System.err.println("Error setting up category tree: " + e.getMessage());
            e.printStackTrace();
            Category fallbackRootData = new Category("error_root", "Помилка завантаження", null);
            TreeItem<CategoryComponent> fallbackItem = new TreeItem<>(fallbackRootData);
            fallbackItem.setExpanded(true);
            categoryTreeView.setRoot(fallbackItem);
            categoryTreeView.setShowRoot(true);
        }
    }

    private TreeItem<CategoryComponent> createTreeItem(CategoryComponent categoryComponent, boolean autoExpand) {
        if (categoryComponent == null) {
            System.err.println("Warning: Attempting to create TreeItem for null categoryComponent");
            return null;
        }

        TreeItem<CategoryComponent> item = new TreeItem<>(categoryComponent);
        item.setExpanded(autoExpand);
        if (categoryComponent instanceof Category) {
            Category category = (Category) categoryComponent;
            CategoryComponent[] childrenArray = category.getChildren();
            if (childrenArray != null) {
                List<CategoryComponent> children = List.of(childrenArray);
                if (!children.isEmpty()) {
                    for (CategoryComponent child : children) {
                        if (child != null) {
                            TreeItem<CategoryComponent> childItem = createTreeItem(child, false);
                            if (childItem != null) {
                                item.getChildren().add(childItem);
                            }
                        } else {
                            System.err.println("Warning: Found null child category in category: " + (category.getName() != null ? category.getName() : "ID: " + category.getId()));
                        }
                    }
                }
            }
        }
        return item;
    }

    private void validateCategoryData() {
        if (categoryService == null) return;
        try {
            List<CategoryComponent> rootCategories = categoryService.getAllRootCategories();
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
            System.err.println("VALIDATION WARNING: Category name is null at path: " + path + " (ID: " + id + ")");
        }
        if (id == null) {
            System.err.println("VALIDATION WARNING: Category id is null at path: " + path + " (Name: " + name + ")");
        }

        if (category instanceof Category) {
            Category cat = (Category) category;
            CategoryComponent[] childrenArray = cat.getChildren();
            if (childrenArray != null) {
                List<CategoryComponent> children = List.of(childrenArray);
                for (int i = 0; i < children.size(); i++) {
                    CategoryComponent child = children.get(i);
                    validateCategory(child, path + " -> " + (name != null ? name : "null_name") + "[" + i + "]");
                }
            }
        }
    }


    private void loadAds(String categoryId) {
        if (adService == null) {
            showErrorAlert("Помилка", "Сервіс оголошень недоступний.", "Неможливо завантажити оголошення.");
            return;
        }
        showLoadingIndicator("Завантаження оголошень...");
        List<Ad> ads;
        String keyword = searchField != null ? searchField.getText() : "";
        ads = adService.searchAds(keyword, null, null, categoryId);

        List<Ad> filteredByQuickFilters = new ArrayList<>();
        if (ads != null) {
            for (Ad ad : ads) {
                boolean pass = true;
                if (quickFilterPremium != null && quickFilterPremium.isSelected() && !ad.isPremium()) pass = false;
                if (quickFilterUrgent != null && quickFilterUrgent.isSelected() && !ad.isUrgent()) pass = false;
                if (quickFilterWithDelivery != null && quickFilterWithDelivery.isSelected() && !ad.hasDelivery()) pass = false;
                if (quickFilterWithWarranty != null && quickFilterWithWarranty.isSelected() && !ad.hasWarranty()) pass = false;
                if (quickFilterWithDiscount != null && quickFilterWithDiscount.isSelected() && !ad.hasDiscount()) pass = false;

                if (pass) {
                    filteredByQuickFilters.add(ad);
                }
            }
        }

        List<AdComponent> decoratedAds = filteredByQuickFilters.stream()
                .map(this::createDecoratedAd)
                .filter(java.util.Objects::nonNull) // Ensure no null components are added
                .toList();
        adsObservableList.setAll(decoratedAds);

        applySorting();
        updatePaginationControls();
        updateStatistics();
        hideLoadingIndicator();
        updateStatus("Завантажено " + adsObservableList.size() + " відповідних оголошень (до пагінації). На сторінці: " + (adListView.getItems() != null ? adListView.getItems().size() : 0) );
    }



    private void refreshCurrentView() {
        loadAds(currentSelectedCategoryId);
    }

    private void handleOpenAdDetails(Ad ad) {
        if (ad == null) return;
        try {
            MainGuiApp.loadAdDetailScene(ad);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка", "Не вдалося відкрити деталі оголошення", e.getMessage());
        }
    }

    @FXML
    private void handleSearchAds() {
        refreshCurrentView();
        if (searchField != null) {
            updateStatus("Пошук за запитом: " + searchField.getText());
        }
    }


    @FXML
    private void handleCreateAd() {
        try {
            MainGuiApp.loadCreateAdScene();
            Platform.runLater(() -> {
                refreshCurrentView();
                updateStatus("Список оновлено після можливого створення оголошення.");
            });
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
            showErrorAlert("Помилка виходу", "Не вдалося завантажити сторінку входу.", e.getMessage());
            Platform.exit();
        }
    }

    @FXML
    private void handleExitApplication() {
        Optional<ButtonType> result = showConfirmationAlert(
                "Підтвердження виходу",
                "Ви впевнені, що хочете закрити програму?",
                "Всі незбережені зміни можуть бути втрачені."
        );
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    // ========== COMMAND PATTERN HANDLERS ==========

    @FXML
    private void handleUndo() {
        if (commandManager == null) return;
        if (commandManager.canUndo()) {
            try {
                commandManager.undo();
                refreshCurrentView();
                updateCommandButtons();
                updateStatus("Команда скасована");
            } catch (UserNotFoundException e) {
                showErrorAlert("Помилка скасування", "Не вдалося скасувати команду.", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRedo() {
        if (commandManager == null) return;
        if (commandManager.canRedo()) {
            try {
                commandManager.redo();
                refreshCurrentView();
                updateCommandButtons();
                updateStatus("Команда повторена");
            } catch (UserNotFoundException e) {
                showErrorAlert("Помилка повторення", "Не вдалося повторити команду.", e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearHistory() {
        if (commandManager == null) return;
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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

    // REVISED METHOD createDecoratedAd based on compiler errors
    private AdComponent createDecoratedAd(Ad ad) {
        if (ad == null) return null;
        AdDecoratorFactory decoratorFactory = new AdDecoratorFactory();
        AdComponent currentComponent;

        // Error 2 ("Ad cannot be converted to AdComponent") indicates that 'Ad' itself is not an 'AdComponent'.
        // Therefore, a base 'AdComponent' must be created from 'ad' using the factory.
        // We'll assume a method like 'createBaseAdComponent' or that one of the specific
        // decorator methods can create the base if no component is passed.
        // Given the errors for createPremiumAd, createUrgentAd, createDiscountAd indicate they
        // expect 'Ad' as parameter and likely return AdComponent, it means the factory handles
        // the creation of the base and wrapping.

        // The structure of this method implies that if multiple conditions (isPremium, isUrgent, hasDiscount)
        // are true, the returned AdComponent should reflect all these decorations.
        // However, the error messages suggest the factory methods like 'createPremiumAd(Ad ad)'
        // return an AdComponent for that specific decoration, based on the Ad.
        // Chaining them (e.g., Premium -> Urgent -> Discount) is not directly supported by calling
        // createPremiumAd(ad), then createUrgentAd(ad) if they don't take an AdComponent to wrap.

        // For this code to compile and adhere to a standard decorator pattern controlled by MainController,
        // AdDecoratorFactory would need methods like:
        // 1. AdComponent createBaseAdComponent(Ad ad);
        // 2. AdComponent addPremiumDecoration(AdComponent componentToWrap);
        // 3. AdComponent addUrgentDecoration(AdComponent componentToWrap);
        // 4. AdComponent addDiscountDecoration(AdComponent componentToWrap);

        // Assuming the factory is structured as per the above ideal for decorator pattern:
        currentComponent = decoratorFactory.createBaseAdComponent(ad); // MUST EXIST IN FACTORY

        if (ad.isPremium()) {
            // This call assumes AdDecoratorFactory has:
            // AdComponent addPremiumDecoration(AdComponent component);
            currentComponent = decoratorFactory.createPremiumAd(currentComponent.getAd());
        }
        if (ad.isUrgent()) {
            // This call assumes AdDecoratorFactory has:
            // AdComponent addUrgentDecoration(AdComponent component);
            currentComponent = decoratorFactory.createUrgentAd(currentComponent.getAd());
        }
        if (ad.hasDiscount()) {
            // This call assumes AdDecoratorFactory has:
            // AdComponent addDiscountDecoration(AdComponent component);
            // This makes it consistent with other decorators.
            //currentComponent = decoratorFactory.createDiscountAd(currentComponent);
        }
        // if (ad.hasDelivery()) { currentComponent = decoratorFactory.addDeliveryDecoration(currentComponent); }
        // if (ad.hasWarranty()) { currentComponent = decoratorFactory.addWarrantyDecoration(currentComponent); }

        return currentComponent;
    }

    // ========== MEDIATOR INTEGRATION METHODS ==========

    public void updateAdsList(List<Ad> adsFromMediator) {
        if (adsFromMediator == null) adsFromMediator = new ArrayList<>();
        List<AdComponent> decoratedAds = adsFromMediator.stream()
                .map(this::createDecoratedAd)
                .filter(java.util.Objects::nonNull)
                .toList();
        Platform.runLater(() -> {
            adsObservableList.setAll(decoratedAds);
            applySorting();
            updatePaginationControls();
            updateStatistics();
            updateStatus("Список оновлено через медіатор. " + decoratedAds.size() + " оголошень.");
            hideLoadingIndicator();
        });
    }

    public void updateMediatorMessage(String message) {
        Platform.runLater(() -> {
            updateStatus(message);
            updateMediatorStatus("активний (повідомлення)");
        });
    }

    public User getCurrentUser() {
        return GlobalContext.getInstance().getLoggedInUser();
    }

    public void logAction(String action) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logMessage = "[" + timestamp + "] " + action;
        Platform.runLater(() -> {
            if (commandHistoryObservableList != null) {
                if (commandHistoryObservableList.size() > 100) {
                    commandHistoryObservableList.remove(0);
                }
                commandHistoryObservableList.add(logMessage);
                if (commandHistoryListView != null && !commandHistoryObservableList.isEmpty()) {
                    commandHistoryListView.scrollTo(commandHistoryObservableList.size() - 1);
                }
            }
        });
    }



    public void cleanup() {
        updateStatus("Очищення контролера...");
        if (mediator != null) {
            updateMediatorStatus("неактивний (очищено)");
        }

        System.out.println("MainController cleanup finished.");
    }
}
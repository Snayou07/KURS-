
package com.example.olx.presentation.gui.controller;

import com.example.olx.domain.decorator.*;
import com.example.olx.presentation.gui.mediator.components.SearchComponent;
import com.example.olx.presentation.gui.mediator.components.AdListComponent;
import com.example.olx.presentation.gui.mediator.components.FilterComponent;
import com.example.olx.application.command.AdCommandManager;
import com.example.olx.application.command.CommandFactory;
import com.example.olx.application.command.CommandInvoker;
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
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.Contract;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
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
    private final ObservableList<AdComponent> adsObservableList = FXCollections.observableArrayList();
    private final ObservableList<String> commandHistoryObservableList = FXCollections.observableArrayList();
    private String currentSelectedCategoryId = null;
    // Додаємо компоненти медіатора
    private AdBrowserMediator mediator;
    private SearchComponent searchComponent;
    private AdListComponent adListComponent;
    // Although declared, not directly used in this controller for its methods
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
            if (createAdButton != null) createAdButton.setDisable(false);
            if (logoutButton != null) logoutButton.setDisable(false);
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
        // Важливо: adListView.setItems(adsObservableList) буде встановлено в updatePaginationControls
        // Тут ми лише налаштовуємо cellFactory
        // adListView.setItems(adsObservableList); // Перенесено в updatePaginationControls

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
                    if (ad.getCategoryId() != null && categoryService != null) {
                        // If getCategoryById() returns Optional<Category>:
                        Optional<Category> categoryOptional = categoryService.getCategoryById(ad.getCategoryId());
                        categoryName = categoryOptional
                                .map(Category::getName)
                                .orElse("ID: " + ad.getCategoryId());
                    } else if (ad.getCategoryId() != null) {
                        categoryName = "ID: " + ad.getCategoryId();
                    }
                    Label categoryInfoLabel = new Label("Категорія: " + categoryName);
                    String dateStr = "Дата: невідома";
                    if (ad.getCreatedAt() != null) {
                        try {
                            dateStr = "Дата: " + DateUtils.formatDate(ad.getCreatedAt());
                        } catch (Exception e) {
                            // Consider logging this exception
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

    // Fix 4: DateUtils Usage
    public static class DateUtils { // Зроблено статичним вкладеним класом для кращої організації

        public static LocalDate toLocalDate(LocalDateTime dateTime) {
            return dateTime != null ? dateTime.toLocalDate() : null;
        }


        public static String formatDate(LocalDateTime dateTime) {
            return dateTime != null ?
                    dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "";
        }


        public static String formatDate(LocalDate date) {
            return date != null ?
                    date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "";
        }
    }
    private void setupGlobalEventListeners() {
        if (categoryTreeView != null) {
            categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValueNode) -> {
                        if (newValueNode != null) {
                            CategoryComponent selectedCategory = newValueNode.getValue();
                            // Fix 1: getName(t) Method Calls (applied to selectedCategory.getName())
                            if (selectedCategory != null && !"root".equals(selectedCategory.getId()) && selectedCategory.getName() != null) {
                                currentSelectedCategoryId = selectedCategory.getId();
                                String categoryName = selectedCategory.getName();
                                if (currentCategoryLabel != null) currentCategoryLabel.setText("Оголошення в категорії: " + categoryName);
                                if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: " + categoryName);
                                if (searchComponent != null) {
                                    searchComponent.updateCategory(currentSelectedCategoryId);
                                    // Inform mediator by triggering search or specific category load
                                    // mediator.loadAdsByCategory(currentSelectedCategoryId); // Приклад
                                    // Або, якщо медіатор реагує на оновлення searchComponent:
                                    searchComponent.performSearch(searchField != null ? searchField.getText() : "", currentSelectedCategoryId);
                                } else {
                                    loadAds(currentSelectedCategoryId);
                                }
                            } else if (selectedCategory != null && "root".equals(selectedCategory.getId())) {
                                // Якщо обрано "Всі категорії"
                                currentSelectedCategoryId = null;
                                if (currentCategoryLabel != null) currentCategoryLabel.setText("Всі оголошення");
                                if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: Всі");
                                if (searchComponent != null) {
                                    searchComponent.updateCategory(null); // або ""
                                    searchComponent.performSearch(searchField != null ? searchField.getText() : "", null);
                                } else {
                                    loadAds(null);
                                }
                            }
                        } else {
                            // Коли вибір скасовано (малоймовірно для TreeView без explicit clearSelection)
                            currentSelectedCategoryId = null;
                            if (currentCategoryLabel != null) currentCategoryLabel.setText("Всі оголошення");
                            if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: немає");
                            if (searchComponent != null) {
                                searchComponent.updateCategory(null);
                                searchComponent.performSearch(searchField != null ? searchField.getText() : "", null);
                            } else {
                                loadAds(null);
                            }
                        }
                    });
        }
    }


    private void setupQuickFilters() {
        // Додаємо перевірку на null для кожного фільтра
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
        if (mediator != null && filterComponent != null) {
            refreshCurrentView();
        } else {
            refreshCurrentView();
        }
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
        adListComponent = new AdListComponent(mediator); // Використовується медіатором для оновлення UI
        filterComponent = new FilterComponent(mediator);
        mediator.registerComponents(searchComponent, adListComponent, filterComponent);

        // ВИПРАВЛЕНО: Розкоментовано. AdBrowserMediator ПОВИНЕН МАТИ ЦЕЙ МЕТОД:
        // public void setController(MainController controller) { this.controller = controller; }
        // Де `private MainController controller;` є полем в AdBrowserMediator.
        // Це дозволить медіатору викликати публічні методи MainController (наприклад, updateAdsList).
        mediator.setController(this);

        System.out.println("Медіатор ініціалізовано успішно.");
        updateMediatorStatus("активний");
    }

    // Fix 5: Mediator Integration
    private void setupMediatorIntegration() {
        if (searchField == null || searchButton == null) { // searchComponent null check is inside actions
            System.err.println("Search UI components (field or button) not initialized for mediator integration.");
            // Fallback to direct handlers if components are missing for mediator setup
            if (searchButton != null) searchButton.setOnAction(e -> handleSearchAds());
            if (searchField != null) searchField.setOnAction(e -> handleSearchAds());
            return;
        }

        // Action for search button
        searchButton.setOnAction(e -> {
            String searchText = searchField.getText();
            if (searchComponent != null) {
                searchComponent.performSearch(searchText, currentSelectedCategoryId);
                updateStatus("Пошук ініційовано через медіатор: " + searchText);
            } else {
                System.err.println("SearchComponent is null, falling back to handleSearchAds()");
                handleSearchAds(); // Fallback if searchComponent is null
            }
        });

        // Action for Enter key in search field
        searchField.setOnAction(e -> {
            String searchText = searchField.getText();
            if (searchComponent != null) {
                searchComponent.performSearch(searchText, currentSelectedCategoryId);
                updateStatus("Пошук ініційовано через медіатор (Enter): " + searchText);
            } else {
                System.err.println("SearchComponent is null, falling back to handleSearchAds()");
                handleSearchAds();
            }
        });


        searchField.textProperty().addListener((observable, oldValue, newValue) -> {

        });
    }



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
        boolean premiumOnlyAdv = premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected(); // Перейменовано, щоб не конфліктувати з quickFilter
        boolean urgentOnlyAdv = urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected(); // Перейменовано

        Double minPrice = null;
        Double maxPrice = null;
        try {
            if (minPriceText != null && !minPriceText.isEmpty()) {
                minPrice = Double.parseDouble(minPriceText);
            }
            if (maxPriceText != null && !maxPriceText.isEmpty()) {
                maxPrice = Double.parseDouble(maxPriceText);
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Помилка фільтрації", "Невірний формат ціни", "Будь ласка, введіть коректні числові значення для ціни.");
            hideLoadingIndicator();
            return;
        }

        String keyword = (searchField != null) ? searchField.getText() : "";

        if (mediator != null && filterComponent != null) {


            loadAdsWithAdvancedFilters(keyword, minPrice, maxPrice, currentSelectedCategoryId, selectedStatus, premiumOnlyAdv, urgentOnlyAdv);
        } else {

            loadAdsWithAdvancedFilters(keyword, minPrice, maxPrice, currentSelectedCategoryId, selectedStatus, premiumOnlyAdv, urgentOnlyAdv);
        }

    }


    private void loadAdsWithAdvancedFilters(String keyword, Double minPrice, Double maxPrice, String categoryId, String status, boolean premiumOnlyAdv, boolean urgentOnlyAdv) {
        if (adService == null) {
            showErrorAlert("Помилка", "Сервіс оголошень недоступний.", "Неможливо завантажити оголошення.");
            hideLoadingIndicator();
            return;
        }
        showLoadingIndicator("Завантаження оголошень з фільтрами...");

        List<Ad> fetchedAds = adService.searchAds(keyword, minPrice, maxPrice, categoryId);

        List<Ad> filteredAds = new ArrayList<>();
        if (fetchedAds != null) {
            for (Ad ad : fetchedAds) {
                if (ad == null) continue; // Fix 6: Null check for elements in list
                boolean statusMatch = "Всі".equals(status) ||
                        (ad.getStatus() != null && status.equals(ad.getStatus().toString()));
                if (!statusMatch) continue;

                // Фільтри з розширеного пошуку
                if (premiumOnlyAdv && !ad.isPremium()) continue;
                if (urgentOnlyAdv && !ad.isUrgent()) continue;

                // Застосовуємо також швидкі фільтри
                if (quickFilterPremium != null && quickFilterPremium.isSelected() && !ad.isPremium()) continue;
                if (quickFilterUrgent != null && quickFilterUrgent.isSelected() && !ad.isUrgent()) continue;
                if (quickFilterWithDelivery != null && quickFilterWithDelivery.isSelected() && !ad.hasDelivery()) continue;
                if (quickFilterWithWarranty != null && quickFilterWithWarranty.isSelected() && !ad.hasWarranty()) continue;
                if (quickFilterWithDiscount != null && quickFilterWithDiscount.isSelected() && !ad.hasDiscount()) continue;

                filteredAds.add(ad);
            }
        }

        List<AdComponent> decoratedAds = filteredAds.stream()
                .map(this::createDecoratedAd)
                .filter(Objects::nonNull) // Ensure no null components are added
                .toList();
        adsObservableList.setAll(decoratedAds); // Оновлюємо основний список
        applySorting(); // Застосовуємо поточне сортування до adsObservableList
        updatePaginationControls(); // Оновлюємо пагінацію, яка візьме дані з adsObservableList
        updateActiveFiltersDisplay();
        updateStatistics();
        hideLoadingIndicator();
        updateStatus("Фільтри застосовано. Знайдено " + decoratedAds.size() + " оголошень (до пагінації).");
    }


    @FXML
    private void handleClearFilters() {
        if (minPriceField != null) minPriceField.clear();
        if (maxPriceField != null) maxPriceField.clear();
        if (statusFilterCombo != null) statusFilterCombo.setValue("Всі");
        if (premiumOnlyCheckBox != null) premiumOnlyCheckBox.setSelected(false);
        if (urgentOnlyCheckBox != null) urgentOnlyCheckBox.setSelected(false);

        refreshCurrentView(); // Це викличе loadAds, який врахує швидкі фільтри
        updateActiveFiltersDisplay();
        updateStatus("Фільтри розширеного пошуку очищено");
    }


    @FXML
    private void handleClearAllFilters() {
        // Спочатку очищаємо поля розширеного пошуку
        if (minPriceField != null) minPriceField.clear();
        if (maxPriceField != null) maxPriceField.clear();
        if (statusFilterCombo != null) statusFilterCombo.setValue("Всі");
        if (premiumOnlyCheckBox != null) premiumOnlyCheckBox.setSelected(false);
        if (urgentOnlyCheckBox != null) urgentOnlyCheckBox.setSelected(false);
        // Потім очищаємо швидкі фільтри
        if (quickFilterPremium != null) quickFilterPremium.setSelected(false);
        if (quickFilterUrgent != null) quickFilterUrgent.setSelected(false);
        if (quickFilterWithDelivery != null) quickFilterWithDelivery.setSelected(false);
        if (quickFilterWithWarranty != null) quickFilterWithWarranty.setSelected(false);
        if (quickFilterWithDiscount != null) quickFilterWithDiscount.setSelected(false);
        // Перезавантажуємо дані без фільтрів (окрім пошукового запиту та категорії, якщо є)
        refreshCurrentView();
        updateActiveFiltersDisplay(); // Це має показати, що активних фільтрів немає
        updateStatus("Всі фільтри очищено");
    }


    @FXML
    private void handleToggleSortOrder() {
        isAscendingSort = !isAscendingSort;
        if (sortOrderButton != null) {
            sortOrderButton.setText(isAscendingSort ? "↑" : "↓");
        }
        applySorting(); // Застосовуємо сортування до adsObservableList
        updatePaginationControls(); // Оновлюємо відображення на поточній сторінці
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
                currentSortBy = "title"; // Fallback to title
                break;
        }
        applySorting(); // Застосовуємо сортування до adsObservableList
        updatePaginationControls(); // Оновлюємо відображення на поточній сторінці
        updateStatus("Сортування змінено на: " + selectedSort);
    }


    private void applySorting() {
        if (adsObservableList != null && !adsObservableList.isEmpty()) {
            // Сортуємо весь список adsObservableList
            adsObservableList.sort((ac1, ac2) -> {
                if (ac1 == null && ac2 == null) return 0;
                if (ac1 == null || ac1.getAd() == null) return isAscendingSort ? 1 : -1; // nulls last or first
                if (ac2 == null || ac2.getAd() == null) return isAscendingSort ? -1 : 1;

                Ad ad1 = ac1.getAd();
                Ad ad2 = ac2.getAd();
                int comparisonResult = 0;

                switch (currentSortBy) {
                    case "title":
                        comparisonResult = Objects.compare(ad1.getTitle(), ad2.getTitle(), String::compareToIgnoreCase);
                        break;
                    case "price":
                        comparisonResult = Double.compare(ad1.getPrice(), ad2.getPrice());
                        break;
                    case "date":
                        comparisonResult = Objects.compare(ad1.getCreatedAt(), ad2.getCreatedAt(), LocalDateTime::compareTo);
                        break;
                    case "popularity":

                        break;
                    default:
                        break;
                }
                return isAscendingSort ? comparisonResult : -comparisonResult;
            });
        }

        System.out.println("Applying sort to full list by: " + currentSortBy + (isAscendingSort ? " ASC" : " DESC"));
    }


    @FXML
    private void handleSwitchToListView() {
        if (listViewButton != null) listViewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        if (gridViewButton != null) gridViewButton.setStyle(""); // Reset style
        // Тут може бути логіка зміни відображення в adListView (наприклад, інший cellFactory)
        updateStatus("Перемкнуто на вигляд списку");
    }


    @FXML
    private void handleSwitchToGridView() {
        if (gridViewButton != null) gridViewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        if (listViewButton != null) listViewButton.setStyle(""); // Reset style
        // Тут може бути логіка зміни відображення в adListView
        updateStatus("Перемкнуто на вигляд сітки");
    }


    @FXML
    private void handleRefresh() {
        showLoadingIndicator("Оновлення...");
        refreshCurrentView(); // Це викличе loadAds
        updateLastUpdateTime();
        // hideLoadingIndicator(); // Вже викликається в loadAds
        updateStatus("Список оновлено");
    }





    @FXML
    private void handleFirstPage() {
        if (currentPage > 1) {
            currentPage = 1;
            // refreshCurrentView(); // Не потрібно перезавантажувати весь список, лише пагінацію
            updatePaginationControls();
            updateStatus("Перехід на першу сторінку");
        }
    }


    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            // refreshCurrentView();
            updatePaginationControls();
            updateStatus("Перехід на попередню сторінку");
        }
    }


    @FXML
    private void handleNextPage() {
        int totalPages = getTotalPages();
        if (currentPage < totalPages) {
            currentPage++;
            // refreshCurrentView();
            updatePaginationControls();
            updateStatus("Перехід на наступну сторінку");
        }
    }


    @FXML
    private void handleLastPage() {
        int totalPages = getTotalPages();
        if (currentPage < totalPages) {
            currentPage = totalPages;
            // refreshCurrentView();
            updatePaginationControls();
            updateStatus("Перехід на останню сторінку");
        }
    }


    private void handlePageSizeChange() {
        if (pageSizeComboBox == null || pageSizeComboBox.getValue() == null) return;
        Integer newPageSize = pageSizeComboBox.getValue();
        if (newPageSize != null && newPageSize > 0 && newPageSize != pageSize) {
            pageSize = newPageSize;
            currentPage = 1; // Скидаємо на першу сторінку при зміні розміру

            updatePaginationControls();
            updateStatus("Розмір сторінки змінено на " + pageSize);
        }
    }

    // Fix 8: Pagination Fixes
    private void updatePaginationControls() {

        if (adsObservableList == null) { // Check if the list itself is null
            if (adListView != null) adListView.setItems(FXCollections.emptyObservableList());
            if (pageInfoLabel != null) pageInfoLabel.setText("Сторінка 0 з 0");
            // Disable all pagination buttons
            if (firstPageButton != null) firstPageButton.setDisable(true);
            if (prevPageButton != null) prevPageButton.setDisable(true);
            if (nextPageButton != null) nextPageButton.setDisable(true);
            if (lastPageButton != null) lastPageButton.setDisable(true);
            if (paginationControls != null) {
                paginationControls.setVisible(false);
                paginationControls.setManaged(false);
            }
            updateStatistics(); // Update stats even if list is null (to show 0)
            return;
        }

        int totalItems = adsObservableList.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        System.out.println("Total items: " + totalItems);


        // Ensure current page is within bounds
        currentPage = Math.max(1, Math.min(currentPage, totalPages));


        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Сторінка " + currentPage + " з " + totalPages);
        }

        if (firstPageButton != null) firstPageButton.setDisable(currentPage <= 1);
        if (prevPageButton != null) prevPageButton.setDisable(currentPage <= 1);
        if (nextPageButton != null) nextPageButton.setDisable(currentPage >= totalPages);
        if (lastPageButton != null) lastPageButton.setDisable(currentPage >= totalPages);

        boolean paginationVisible = totalPages > 1;
        if (paginationControls != null) {
            paginationControls.setVisible(paginationVisible);
            paginationControls.setManaged(paginationVisible);
        }

        if (adListView != null) {
            if (totalItems == 0) {
                adListView.setItems(FXCollections.emptyObservableList());
            } else {
                int fromIndex = (currentPage - 1) * pageSize;
                int toIndex = Math.min(fromIndex + pageSize, totalItems);

                // Додано перевірку індексів для subList
                if (fromIndex >= 0 && fromIndex < totalItems && toIndex <= totalItems && fromIndex <= toIndex) { // fromIndex can be equal to toIndex for empty sublist
                    List<AdComponent> pageData = adsObservableList.subList(fromIndex, toIndex);
                    adListView.setItems(FXCollections.observableArrayList(pageData));
                } else if (fromIndex >= totalItems && totalItems > 0) { // If fromIndex is out of bounds but list not empty (e.g. after deletions)
                    // Go to last valid page
                    currentPage = totalPages;
                    fromIndex = (currentPage - 1) * pageSize;
                    toIndex = Math.min(fromIndex + pageSize, totalItems);
                    if(fromIndex < toIndex) { // Check again
                        List<AdComponent> pageData = adsObservableList.subList(fromIndex, toIndex);
                        adListView.setItems(FXCollections.observableArrayList(pageData));
                    } else {
                        adListView.setItems(FXCollections.emptyObservableList());
                    }
                } else if (totalItems == 0 || fromIndex >= toIndex) { // List is empty or became empty for current page
                    adListView.setItems(FXCollections.emptyObservableList());
                }
                else {
                    // This case should ideally not happen with correct currentPage management
                    System.err.println("Pagination error: fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", totalItems=" + totalItems + ", currentPage=" + currentPage);
                    adListView.setItems(FXCollections.emptyObservableList()); // or show first page
                }
            }
        }
        updateStatistics();
    }


    private int getTotalPages() {
        if (adsObservableList == null || adsObservableList.isEmpty() || pageSize <= 0) return 1;
        int totalAds = adsObservableList.size();
        return Math.max(1, (int) Math.ceil((double) totalAds / pageSize));
    }



    private void updateActiveFiltersDisplay() {
        if (activeFiltersContainer == null || activeFiltersPanel == null) return;
        activeFiltersContainer.getChildren().clear();
        boolean hasActiveFilters = false;

        String minPriceText = (minPriceField != null) ? minPriceField.getText() : "";
        if (minPriceText != null && !minPriceText.isEmpty()) {
            addFilterChip("Мін. ціна: " + minPriceText);
            hasActiveFilters = true;
        }
        String maxPriceText = (maxPriceField != null) ? maxPriceField.getText() : "";
        if (maxPriceText != null && !maxPriceText.isEmpty()) {
            addFilterChip("Макс. ціна: " + maxPriceText);
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
        if (totalAdsLabel != null) {
            // totalAdsLabel показує загальну кількість завантажених/відфільтрованих оголошень (весь список)
            totalAdsLabel.setText("Всього (фільтр.): " + (adsObservableList != null ? adsObservableList.size() : 0));
        }

        if (filteredAdsLabel != null && adListView != null && adListView.getItems() != null) {
            // filteredAdsLabel показує кількість оголошень на поточній сторінці
            filteredAdsLabel.setText("На сторінці: " + adListView.getItems().size());
        }

        if (selectedCategoryLabel != null) {
            if (currentSelectedCategoryId == null) {
                selectedCategoryLabel.setText("Обрана категорія: Всі");
            } else if (categoryTreeView != null && categoryTreeView.getSelectionModel().getSelectedItem() != null) {
                CategoryComponent selectedComp = categoryTreeView.getSelectionModel().getSelectedItem().getValue();
                if (selectedComp != null) {
                    // Fix 1: getName(t) Method Calls
                    selectedCategoryLabel.setText("Обрана категорія: " + selectedComp.getName());
                }
            } else if (currentCategoryLabel != null && !currentCategoryLabel.getText().equals("Всі оголошення")) {
                // Fallback if tree selection is somehow lost but category context exists
                selectedCategoryLabel.setText(currentCategoryLabel.getText().replace("Оголошення в категорії: ", "Обрана категорія: "));
            }
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

    // Fix 9: Loading Indicator
    private void showLoadingIndicator(String message) {
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(true);
                loadingIndicator.setManaged(true);
            }
            if (loadingLabel != null) {
                loadingLabel.setText(message != null ? message : "Завантаження...");
            }
        });
    }


    private void hideLoadingIndicator() {
        Platform.runLater(() -> { // Ensure UI updates are on FX thread
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
            }
            if (loadingLabel != null) {
                loadingLabel.setText("");
            }
        });
    }




    private void initializeCommandManager() {
        if (adService == null) {
            showErrorAlert("Критична помилка", "AdService не ініціалізовано.", "Неможливо створити CommandManager.");
            return;
        }
        CommandInvoker commandInvoker = new CommandInvoker();
        // Переконайтесь, що MainGuiApp.adService ініціалізовано до цього моменту
        CommandFactory commandFactoryInstance = new CommandFactory(MainGuiApp.adService);
        commandManager = new AdCommandManager(commandInvoker, commandFactoryInstance);
    }


    private void setupCommandHistoryView() {
        if (commandHistoryListView != null) {
            commandHistoryListView.setItems(commandHistoryObservableList);
            commandHistoryListView.setPrefHeight(150); // Можна налаштувати за потреби
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
        if (commandHistoryObservableList != null && commandManager != null) { // Додано перевірку commandManager
            commandHistoryObservableList.setAll(commandManager.getCommandHistory());
        }
    }


    private void setupCategoryTree() {
        if (categoryTreeView == null || categoryService == null) {
            System.err.println("Error: categoryTreeView or categoryService is null. Cannot setup category tree.");
            if (categoryTreeView != null) { // Створюємо fallback лише якщо сам TreeView існує
                TreeItem<CategoryComponent> fallbackRoot = new TreeItem<>(new Category("fallback", "Помилка завантаження категорій", null));
                categoryTreeView.setRoot(fallbackRoot);
            }
            return;
        }

        try {
            List<CategoryComponent> rootCategories = categoryService.getAllRootCategories();
            if (rootCategories == null) { // Додаткова перевірка на null
                rootCategories = new ArrayList<>();
                System.err.println("Warning: CategoryService.getAllRootCategories() returned null. Using empty list.");
            }

            // Створюємо штучний кореневий вузол "Всі категорії"
            Category allCategoriesDataNode = new Category("root", "Всі категорії", null);
            TreeItem<CategoryComponent> rootTreeItem = new TreeItem<>(allCategoriesDataNode);
            rootTreeItem.setExpanded(true);

            for (CategoryComponent rootCategory : rootCategories) {
                if (rootCategory != null) { // Перевірка на null перед створенням
                    TreeItem<CategoryComponent> categoryItem = createTreeItem(rootCategory, true); // autoExpand може бути false для кореневих
                    if (categoryItem != null) {
                        rootTreeItem.getChildren().add(categoryItem);
                    }
                } else {
                    System.err.println("Warning: Found null root category, skipping...");
                }
            }

            categoryTreeView.setRoot(rootTreeItem);
            categoryTreeView.setShowRoot(true); // Показуємо "Всі категорії"

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
        } catch (Exception e) { // Ловимо ширший спектр винятків
            System.err.println("Error setting up category tree: " + e.getMessage());
            e.printStackTrace(); // Важливо для діагностики

            Category fallbackRootData = new Category("error_root", "Помилка завантаження", null);
            TreeItem<CategoryComponent> fallbackItem = new TreeItem<>(fallbackRootData);
            fallbackItem.setExpanded(true);
            if (categoryTreeView != null) { // Перевіряємо, чи сам TreeView не null
                categoryTreeView.setRoot(fallbackItem);
                categoryTreeView.setShowRoot(true);
            }
        }
    }


    private TreeItem<CategoryComponent> createTreeItem(CategoryComponent categoryComponent, boolean autoExpand) {
        if (categoryComponent == null) {
            System.err.println("Warning: Attempting to create TreeItem for null categoryComponent, skipping.");
            return null;
        }

        TreeItem<CategoryComponent> item = new TreeItem<>(categoryComponent);
        item.setExpanded(autoExpand);

        if (categoryComponent instanceof Category) { // Уточнення типу
            Category category = (Category) categoryComponent;
            CategoryComponent[] childrenArray = category.getChildren(); // Припускаємо, що такий метод є в Category

            if (childrenArray != null) {
                for (CategoryComponent childComp : childrenArray) {
                    if (childComp != null) { // Ensure child component itself is not null
                        TreeItem<CategoryComponent> childItem = createTreeItem(childComp, false); // autoExpand false для підкатегорій
                        if (childItem != null) { // Ensure the created tree item for child is not null
                            item.getChildren().add(childItem);
                        }
                    } else {
                        System.err.println("Warning: Found null child category in category: " +
                                (category.getName() != null ? category.getName() : "ID: " + category.getId()) + ", skipping child.");
                    }
                }
            }
        }
        return item;
    }


    private void validateCategoryData() { /* ... без змін ... */ }
    private void validateCategory(CategoryComponent category, String path) { /* ... без змін ... */ }


    private void loadAds(String categoryId) {
        System.out.println("Loading ads for category: " + categoryId);
        if (adService == null) {
            showErrorAlert("Помилка", "Сервіс оголошень недоступний.", "Неможливо завантажити оголошення.");
            hideLoadingIndicator(); // Важливо сховати індикатор у разі помилки
            return;
        }
        showLoadingIndicator("Завантаження оголошень...");
        List<Ad> ads;
        String keyword = (searchField != null) ? searchField.getText() : "";
        ads = adService.searchAds(keyword, null, null, categoryId);
        List<Ad> filteredByQuickFilters = new ArrayList<>();
        if (ads != null) { // Додано перевірку на null для ads
            for (Ad ad : ads) {
                if (ad == null) continue; // Пропускаємо null оголошення
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
        } else {
            ads = new ArrayList<>(); // Якщо сервіс повернув null, працюємо з порожнім списком
        }


        List<AdComponent> decoratedAds = filteredByQuickFilters.stream()
                .map(this::createDecoratedAd)
                .filter(Objects::nonNull) // Фільтруємо null компоненти після декорації
                .toList();
        adsObservableList.setAll(decoratedAds); // Оновлюємо повний список

        applySorting(); // Сортуємо повний список
        updatePaginationControls(); // Оновлюємо пагінацію (яка візьме дані з adsObservableList)
        updateActiveFiltersDisplay(); // Оновлюємо відображення активних фільтрів
        updateStatistics(); // Оновлюємо статистику
        hideLoadingIndicator();
        System.out.println("Total ads loaded: " + ads.size());
        System.out.println("After filtering: " + filteredByQuickFilters.size());
        System.out.println("After decoration: " + decoratedAds.size());
        updateStatus("Завантажено " + adsObservableList.size() + " відповідних оголошень (до пагінації). На сторінці: " + (adListView != null && adListView.getItems() != null ? adListView.getItems().size() : 0) );
    }



    private void refreshCurrentView() {
        loadAds(currentSelectedCategoryId);

        boolean advancedFiltersActive = (minPriceField != null && !minPriceField.getText().isEmpty()) ||
                (maxPriceField != null && !maxPriceField.getText().isEmpty()) ||
                (statusFilterCombo != null && statusFilterCombo.getValue() != null && !"Всі".equals(statusFilterCombo.getValue())) ||
                (premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected()) ||
                (urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected());

        if (advancedFiltersActive) {
            handleApplyFilters(); // Це викличе loadAdsWithAdvancedFilters
        } else {
            loadAds(currentSelectedCategoryId); // Це врахує швидкі фільтри
        }


    }

    // handleOpenAdDetails - без змін
    private void handleOpenAdDetails(Ad ad) { /* ... без змін ... */ }

    @FXML
    private void handleSearchAds() {
        refreshCurrentView();
        if (searchField != null) {
            updateStatus("Пошук за запитом: " + searchField.getText());
        }
    }

    @FXML
    public void handleCreateAd() {
        try {
            MainGuiApp.loadCreateAdScene();
            // Після повернення зі сцени створення оновіть список
            refreshCurrentView();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка", "Не вдалося відкрити форму створення оголошення");
        }
    }

    @FXML
    public void handleLogout() {
        try {
            MainGuiApp.loadLoginScene();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка", "Не вдалося перейти до екрану входу");
        }
    }

    @FXML
    public void handleExitApplication() {
        try {

            if (MainGuiApp.sessionManager != null) {
                MainGuiApp.sessionManager.saveState();
            }


            Platform.exit();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
            System.exit(0);
        }
    }


    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    @FXML
    private void handleUndo() {
        try {
            if (commandManager != null && commandManager.canUndo()) {
                commandManager.undo();
                updateCommandButtons();
                refreshCurrentView(); // Refresh data to reflect undone action
                logAction("Дію скасовано (Undo).");
                updateStatus("Попередню дію скасовано.");
            } else {
                if (undoButton != null) undoButton.setDisable(true); // Ensure button state is correct
            }
        } catch (Exception e) { // Catching a more general exception as UserNotFoundException might not be the only one
            e.printStackTrace();
            showErrorAlert("Помилка скасування", "Не вдалося скасувати попередню дію.", e.getMessage());
        }
    }

    @FXML
    private void handleRedo() {
        try {
            if (commandManager != null && commandManager.canRedo()) {
                commandManager.redo();
                updateCommandButtons();
                refreshCurrentView(); // Refresh data to reflect redone action
                logAction("Дію повторено (Redo).");
                updateStatus("Скасовану дію повторено.");
            } else {
                if (redoButton != null) redoButton.setDisable(true); // Ensure button state is correct
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Помилка повторення", "Не вдалося повторити скасовану дію.", e.getMessage());
        }
    }

    @FXML
    private void handleClearHistory() {
        try {
            if (commandManager != null) {
                commandManager.clearHistory();
                updateCommandButtons();
                logAction("Історію команд очищено.");
                updateStatus("Історію команд очищено.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Помилка очищення історії", "Не вдалося очистити історію команд.", e.getMessage());
        }
    }



    @Contract(pure = true)
    private void showError(String message) {
        if (message == null || message.trim().isEmpty()) {
            System.err.println("Помилка: порожнє повідомлення про помилку");
            return;
        }

        System.err.println("ПОМИЛКА: " + message);

        // Також можна додати логування у файл, якщо потрібно
        Logger.getLogger(this.getClass().getName()).severe(message);
    }

    @Contract(pure = true)
    private void showErrorAlert(String title, String header, String content) {
        if (title == null) title = "Помилка";
        if (header == null) header = "Виникла помилка";
        if (content == null || content.trim().isEmpty()) {
            content = "Невідома помилка";
        }

        String finalContent = content;
        String finalHeader = header;
        String finalTitle = title;
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(finalTitle);
            alert.setHeaderText(finalHeader);
            alert.setContentText(finalContent);

            // Налаштування іконки та стилю
            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/styles/alert-styles.css").toExternalForm()
            );

            alert.showAndWait();
        });
    }

    @Contract(pure = true)
    private Optional<ButtonType> showConfirmationAlert(String title, String header, String content) {
        if (title == null) title = "Підтвердження";
        if (header == null) header = "Підтвердьте дію";
        if (content == null || content.trim().isEmpty()) {
            content = "Ви впевнені, що хочете продовжити?";
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Налаштування кнопок
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        // Налаштування тексту кнопок українською
        Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
        Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
        yesButton.setText("Так");
        noButton.setText("Ні");

        // Налаштування стилю
        try {
            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/styles/alert-styles.css").toExternalForm()
            );
        } catch (Exception e) {
            // Ігноруємо помилку стилів
        }

        return alert.showAndWait();
    }

    @Contract(pure = true)
    private void showInfoAlert(String title, String header, String content) {
        if (title == null) title = "Інформація";
        if (header == null) header = "Повідомлення";
        if (content == null || content.trim().isEmpty()) {
            content = "Немає додаткової інформації";
        }

        String finalTitle = title;
        String finalHeader = header;
        String finalContent = content;
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(finalTitle);
            alert.setHeaderText(finalHeader);
            alert.setContentText(finalContent);

            // Налаштування кнопки
            Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("Гаразд");

            // Налаштування стилю
            try {
                alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("/styles/alert-styles.css").toExternalForm()
                );
            } catch (Exception e) {
                // Ігноруємо помилку стилів
            }

            alert.showAndWait();
        });
    }

// Додаткові utility методи для зручності

    @Contract(pure = true)
    private void showSuccessAlert(String message) {
        showInfoAlert("Успіх", "Операція виконана успішно", message);
    }

    @Contract(pure = true)
    private void showWarningAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title != null ? title : "Попередження");
            alert.setHeaderText("Увага!");
            alert.setContentText(message != null ? message : "Виявлено потенційну проблему");

            Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("Зрозуміло");

            alert.showAndWait();
        });
    }

    @Contract(pure = true)
    private boolean confirmAction(String message) {
        Optional<ButtonType> result = showConfirmationAlert(
                "Підтвердження",
                "Підтвердьте дію",
                message
        );
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    // Метод для відображення помилок з exception
    @Contract(pure = true)
    private void showErrorAlert(String title, Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = "Виникла невідома помилка: " + exception.getClass().getSimpleName();
        }

        showErrorAlert(title, "Помилка виконання", message);

        // Логуємо повний stack trace
        exception.printStackTrace();
    }

    private AdComponent createDecoratedAd(Ad ad) {
        if (ad == null) {
            System.out.println("Attempted to decorate null ad");
            return null;
        }
        System.out.println("Decorating ad: " + ad.getTitle() + " (ID: " + ad.getId() + ")");
        if (ad == null) return null;
        AdComponent currentComponent = AdDecoratorFactory.createBasicAd(ad);

        if (ad.hasDiscount()) {
            double discountPercentage = ad.getDiscountPercentage();
            String discountReason = ad.getDiscountReason();
            currentComponent = new DiscountAdDecorator(currentComponent, discountPercentage, discountReason);
        }

        if (ad.hasWarranty()) {
            int warrantyMonths = ad.getWarrantyMonths();
            String warrantyType = ad.getWarrantyType();
            currentComponent = new WarrantyAdDecorator(currentComponent, warrantyMonths, warrantyType);
        }

        if (ad.hasDelivery()) {
            boolean freeDelivery = ad.isFreeDelivery();
            double deliveryCost = ad.getDeliveryCost();
            String deliveryInfo = ad.getDeliveryInfo();
            currentComponent = new DeliveryAdDecorator(currentComponent, freeDelivery, deliveryCost, deliveryInfo);
        }

        if (ad.isUrgent()) {
            currentComponent = new UrgentAdDecorator(currentComponent);
        }

        if (ad.isPremium()) {
            currentComponent = new PremiumAdDecorator(currentComponent);
        }

        return currentComponent;
    }

    private AdComponent createDecoratedAdAlternative(Ad ad) {
        if (ad == null) return null;

        return AdDecoratorFactory.createFullyDecoratedAd(
                ad,
                ad.isPremium(),
                ad.isUrgent(),
                ad.hasDiscount() ? ad.getDiscountPercentage() : null,
                ad.hasDiscount() ? ad.getDiscountReason() : null,
                ad.hasWarranty() ? ad.getWarrantyMonths() : null,
                ad.hasWarranty() ? ad.getWarrantyType() : null,
                ad.hasDelivery() ? ad.isFreeDelivery() : null,
                ad.hasDelivery() ? ad.getDeliveryCost() : null,
                ad.hasDelivery() ? ad.getDeliveryInfo() : null
        );
    }

    public void updateAdsList(List<Ad> adsFromMediator) {
        System.out.println("Received ads from mediator: " + adsFromMediator.size());
        if (adsFromMediator == null) {
            System.out.println("Mediator sent null ads list");
            adsFromMediator = new ArrayList<>();
        }

        List<AdComponent> decoratedAds = adsFromMediator.stream()
                .peek(ad -> System.out.println("Processing ad: " + ad.getId() + " - " + ad.getTitle() + " (" + ad.getStatus() + ")"))
                .map(this::createDecoratedAd)
                .filter(Objects::nonNull)
                .toList();

        System.out.println("Decorated ads count: " + decoratedAds.size());

        Platform.runLater(() -> {
            System.out.println("Updating UI with " + decoratedAds.size() + " ads");
            adsObservableList.setAll(decoratedAds);
            applySorting();
            updatePaginationControls();
            updateActiveFiltersDisplay();
            updateStatus("Оновлено " + decoratedAds.size() + " оголошень");
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
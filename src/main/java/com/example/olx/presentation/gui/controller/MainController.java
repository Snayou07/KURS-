package com.example.olx.presentation.gui.controller;

import com.example.olx.domain.decorator.*;
import com.example.olx.presentation.gui.mediator.components.SearchComponent;
import com.example.olx.presentation.gui.mediator.components.AdListComponent;
import com.example.olx.presentation.gui.mediator.components.FilterComponent;
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

    // Сортування та відображення
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button sortOrderButton;
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

    private final ObservableList<AdComponent> adsObservableList = FXCollections.observableArrayList();
    private String currentSelectedCategoryId = null;
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
            if (createAdButton != null) createAdButton.setDisable(false);
            if (logoutButton != null) logoutButton.setDisable(false);
        } else {
            try {
                MainGuiApp.loadLoginScene();
                return;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to load login scene", e);
                showErrorAlert("Помилка входу", "Не вдалося завантажити сторінку входу.", e.getMessage());
                Platform.exit();
                return;
            }
        }

        initializeMediator();
        initializeUIComponents();
        setupCategoryTree();
        setupAdListView();
        setupMediatorIntegration();
        setupGlobalEventListeners();

        if (this.mediator != null) {
            this.mediator.loadAllAds();
        } else {
            loadAds(null);
        }

        updateLastUpdateTime();
        updateStatus("Головне вікно ініціалізовано.");
    }

    private void initializeUIComponents() {
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
                "За назвою", "За ціною", "За датою", "За популярністю"
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

        if (advancedSearchPanel != null) {
            advancedSearchPanel.setVisible(false);
            advancedSearchPanel.setManaged(false);
        }
    }

    private void setupAdListView() {
        if (adListView == null) {
            LOGGER.severe("Error: adListView is null. Check FXML binding.");
            return;
        }
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

                    // --- ВИПРАВЛЕННЯ: ОЧИЩЕННЯ ОПИСУ ВІД ТЕХНІЧНИХ ДАНИХ ---
                    String rawDescription = ad.getDescription(); //
                    String cleanDescription = rawDescription;

                    // Перевіряємо, чи є в описі наш маркер, і видаляємо все після нього
                    if (rawDescription != null && rawDescription.contains("[DECORATORS]")) {
                        int decoratorIndex = rawDescription.indexOf("[DECORATORS]");
                        cleanDescription = rawDescription.substring(0, decoratorIndex).trim();
                    }

                    if (cleanDescription != null && cleanDescription.length() > 100) {
                        cleanDescription = cleanDescription.substring(0, 100) + "...";
                    }
                    // Використовуємо очищений опис. Якщо він порожній, виводимо "Немає опису".
                    Label descLabel = new Label(cleanDescription != null && !cleanDescription.isEmpty() ? cleanDescription : "Немає опису"); //
                    descLabel.setStyle("-fx-text-fill: #666666;"); //

                    HBox infoBox = new HBox(15);

                    String categoryName = "Невідомо";
                    if (ad.getCategoryId() != null && categoryService != null) { //
                        Optional<Category> categoryOptional = categoryService.getCategoryById(ad.getCategoryId()); //
                        categoryName = categoryOptional
                                .map(Category::getName)
                                .orElse("ID: " + ad.getCategoryId()); //
                    } else if (ad.getCategoryId() != null) { //
                        categoryName = "ID: " + ad.getCategoryId(); //
                    }
                    Label categoryInfoLabel = new Label("Категорія: " + categoryName); //
                    String dateStr = "Дата: невідома"; //
                    if (ad.getCreatedAt() != null) { //
                        try {
                            dateStr = "Дата: " + DateUtils.formatDate(ad.getCreatedAt()); //
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error formatting date for ad: " + ad.getId(), e); //
                            dateStr = "Дата: " + (ad.getCreatedAt() != null ?
                                    ad.getCreatedAt().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) :
                                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))) +
                                    " (fallback format error)"; //
                        }
                    }
                    Label dateLabel = new Label(dateStr); //
                    infoBox.getChildren().addAll(categoryInfoLabel, dateLabel); //

                    container.getChildren().addAll(titleLabel, priceLabel, descLabel, infoBox);

                    // --- ЛОГІКА ВІДОБРАЖЕННЯ ДЕКОРАТОРІВ (залишається без змін) ---
                    List<String> decoratorInfoParts = new ArrayList<>(); //
                    if (ad.isPremium()) { //
                        decoratorInfoParts.add("⭐ Преміум"); //
                    }
                    if (ad.isUrgent()) { //
                        decoratorInfoParts.add("🔥 Терміново"); //
                    }
                    if (ad.hasDelivery()) { //
                        String deliveryInfo = "🚚 Доставка"; //
                        if (ad.isFreeDelivery()) { //
                            deliveryInfo += " (безкоштовна)"; //
                        }
                        decoratorInfoParts.add(deliveryInfo); //
                    }
                    if (ad.hasWarranty()) { //
                        decoratorInfoParts.add("🛡️ Гарантія"); //
                    }
                    if (ad.hasDiscount()) { //
                        decoratorInfoParts.add(String.format("💲 Знижка %.0f%%", ad.getDiscountPercentage())); //
                    }

                    if (!decoratorInfoParts.isEmpty()) { //
                        String decoratorText = String.join(" | ", decoratorInfoParts); //
                        Label decoratedInfoLabel = new Label(decoratorText); //
                        decoratedInfoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: blue; -fx-padding: 5 0 0 0;"); //
                        container.getChildren().add(decoratedInfoLabel); //
                    }

                    setGraphic(container); //
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
                        LOGGER.log(Level.SEVERE, "Error opening ad details", e);
                        showError("Помилка відкриття деталей оголошення: " + e.getMessage());
                    }
                }
            }
        });
    }

    public static class DateUtils {
        public static LocalDate toLocalDate(LocalDateTime dateTime) {
            return dateTime != null ?
                    dateTime.toLocalDate() : null;
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
                            if (selectedCategory != null && !"root".equals(selectedCategory.getId()) && selectedCategory.getName() != null) {
                                currentSelectedCategoryId = selectedCategory.getId();
                                String categoryName = selectedCategory.getName();
                                if (currentCategoryLabel != null) currentCategoryLabel.setText("Оголошення в категорії: " + categoryName);
                                if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: " + categoryName);
                                if (searchComponent != null) {
                                    searchComponent.updateCategory(currentSelectedCategoryId);
                                    searchComponent.performSearch(searchField != null ? searchField.getText() : "", currentSelectedCategoryId);
                                } else {
                                    loadAds(currentSelectedCategoryId);
                                }
                            } else if (selectedCategory != null && "root".equals(selectedCategory.getId())) {
                                currentSelectedCategoryId = null;
                                if (currentCategoryLabel != null) currentCategoryLabel.setText("Всі оголошення");
                                if (selectedCategoryLabel != null) selectedCategoryLabel.setText("Обрана категорія: Всі");
                                if (searchComponent != null) {
                                    searchComponent.updateCategory(null);
                                    searchComponent.performSearch(searchField != null ? searchField.getText() : "", null);
                                } else {
                                    loadAds(null);
                                }
                            }
                        } else {
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
        mediator.setController(this);

        LOGGER.info("Медіатор ініціалізовано успішно.");
        updateMediatorStatus("активний");
    }

    private void setupMediatorIntegration() {
        if (searchField == null || searchButton == null) {
            LOGGER.warning("Search UI components (field or button) not initialized for mediator integration.");
            if (searchButton != null) searchButton.setOnAction(e -> handleSearchAds());
            if (searchField != null) searchField.setOnAction(e -> handleSearchAds());
            return;
        }

        searchButton.setOnAction(e -> {
            String searchText = searchField.getText();
            if (searchComponent != null) {
                searchComponent.performSearch(searchText, currentSelectedCategoryId);
                updateStatus("Пошук ініційовано через медіатор: " + searchText);
            } else {
                LOGGER.warning("SearchComponent is null, falling back to handleSearchAds()");
                handleSearchAds();
            }
        });
        searchField.setOnAction(e -> {
            String searchText = searchField.getText();
            if (searchComponent != null) {
                searchComponent.performSearch(searchText, currentSelectedCategoryId);
                updateStatus("Пошук ініційовано через медіатор (Enter): " + searchText);
            } else {
                LOGGER.warning("SearchComponent is null, falling back to handleSearchAds()");
                handleSearchAds();
            }
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
        String minPriceText = (minPriceField != null) ?
                minPriceField.getText() : "";
        String maxPriceText = (maxPriceField != null) ? maxPriceField.getText() : "";
        String selectedStatus = (statusFilterCombo != null && statusFilterCombo.getValue() != null) ? statusFilterCombo.getValue() : "Всі";
        boolean premiumOnlyAdv = premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected();
        boolean urgentOnlyAdv = urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected();

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
            return;
        }

        String keyword = (searchField != null) ? searchField.getText() : "";
        loadAdsWithAdvancedFilters(keyword, minPrice, maxPrice, currentSelectedCategoryId, selectedStatus, premiumOnlyAdv, urgentOnlyAdv);
    }

    private void loadAdsWithAdvancedFilters(String keyword, Double minPrice, Double maxPrice, String categoryId, String status, boolean premiumOnlyAdv, boolean urgentOnlyAdv) {
        if (adService == null) {
            showErrorAlert("Помилка", "Сервіс оголошень недоступний.", "Неможливо завантажити оголошення.");
            return;
        }
        showLoadingIndicator("Завантаження оголошень з фільтрами...");
        Task<List<AdComponent>> loadTask = new Task<>() {
            @Override
            protected List<AdComponent> call() throws Exception {
                List<Ad> fetchedAds = adService.searchAds(keyword, minPrice, maxPrice, categoryId);
                List<Ad> filteredAds = new ArrayList<>();
                if (fetchedAds != null) {
                    for (Ad ad : fetchedAds) {
                        if (ad == null) continue;
                        boolean statusMatch = "Всі".equals(status) || (ad.getStatus() != null && status.equals(ad.getStatus().toString()));
                        if (!statusMatch) continue;

                        if (premiumOnlyAdv && !ad.isPremium()) continue;
                        if (urgentOnlyAdv && !ad.isUrgent()) continue;

                        filteredAds.add(ad);
                    }
                }
                return filteredAds.stream()
                        .map(MainController.this::createDecoratedAd)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        };
        loadTask.setOnSucceeded(event -> {
            List<AdComponent> decoratedAds = loadTask.getValue();
            adsObservableList.setAll(decoratedAds);
            applySorting();
            updatePaginationControls();
            updateActiveFiltersDisplay();
            updateStatistics();
            hideLoadingIndicator();
            updateStatus("Фільтри застосовано. Знайдено " + decoratedAds.size() + " оголошень (до пагінації).");
        });
        loadTask.setOnFailed(event -> {
            LOGGER.log(Level.SEVERE, "Failed to load ads with advanced filters", loadTask.getException());
            hideLoadingIndicator();
            showErrorAlert("Помилка завантаження", "Не вдалося завантажити оголошення з фільтрами.", loadTask.getException().getMessage());
        });
        new Thread(loadTask).start();
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
        if (minPriceField != null) minPriceField.clear();
        if (maxPriceField != null) maxPriceField.clear();
        if (statusFilterCombo != null) statusFilterCombo.setValue("Всі");
        if (premiumOnlyCheckBox != null) premiumOnlyCheckBox.setSelected(false);
        if (urgentOnlyCheckBox != null) urgentOnlyCheckBox.setSelected(false);
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
        updatePaginationControls();
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
            case "За популярністю":
                currentSortBy = "popularity";
                break;
            default:
                currentSortBy = "title";
                break;
        }
        applySorting();
        updatePaginationControls();
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
                        comparisonResult = Objects.compare(ad1.getTitle(), ad2.getTitle(), String::compareToIgnoreCase);
                        break;
                    case "price":
                        comparisonResult = Double.compare(ad1.getPrice(), ad2.getPrice());
                        break;
                    case "date":
                        comparisonResult = Objects.compare(ad1.getCreatedAt(), ad2.getCreatedAt(), LocalDateTime::compareTo);
                        break;
                    case "popularity":
                        if (ad1.getCreatedAt() != null && ad2.getCreatedAt() != null) {
                            comparisonResult = ad2.getCreatedAt().compareTo(ad1.getCreatedAt());
                        } else {
                            comparisonResult = Objects.compare(ad1.getCreatedAt(), ad2.getCreatedAt(), Comparator.nullsLast(LocalDateTime::compareTo));
                        }
                        break;
                    default:
                        break;
                }
                return isAscendingSort ?
                        comparisonResult : -comparisonResult;
            });
        }
        LOGGER.info("Applying sort to full list by: " + currentSortBy + (isAscendingSort ? " ASC" : " DESC"));
    }

    @FXML
    private void handleRefresh() {
        refreshCurrentView();
        updateLastUpdateTime();
        updateStatus("Список оновлено");
    }

    @FXML
    private void handleFirstPage() {
        if (currentPage > 1) {
            currentPage = 1;
            updatePaginationControls();
            updateStatus("Перехід на першу сторінку");
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePaginationControls();
            updateStatus("Перехід на попередню сторінку");
        }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = getTotalPages();
        if (currentPage < totalPages) {
            currentPage++;
            updatePaginationControls();
            updateStatus("Перехід на наступну сторінку");
        }
    }

    @FXML
    private void handleLastPage() {
        int totalPages = getTotalPages();
        if (currentPage < totalPages) {
            currentPage = totalPages;
            updatePaginationControls();
            updateStatus("Перехід на останню сторінку");
        }
    }

    private void handlePageSizeChange() {
        if (pageSizeComboBox == null || pageSizeComboBox.getValue() == null) return;
        Integer newPageSize = pageSizeComboBox.getValue();
        if (newPageSize != null && newPageSize > 0 && newPageSize != pageSize) {
            pageSize = newPageSize;
            currentPage = 1;
            updatePaginationControls();
            updateStatus("Розмір сторінки змінено на " + pageSize);
        }
    }

    private void updatePaginationControls() {
        if (adsObservableList == null) {
            if (adListView != null) adListView.setItems(FXCollections.emptyObservableList());
            if (pageInfoLabel != null) pageInfoLabel.setText("Сторінка 0 з 0");
            if (firstPageButton != null) firstPageButton.setDisable(true);
            if (prevPageButton != null) prevPageButton.setDisable(true);
            if (nextPageButton != null) nextPageButton.setDisable(true);
            if (lastPageButton != null) lastPageButton.setDisable(true);
            if (paginationControls != null) {
                paginationControls.setVisible(false);
                paginationControls.setManaged(false);
            }
            updateStatistics();
            return;
        }

        int totalItems = adsObservableList.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
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

                if (fromIndex >= 0 && fromIndex < totalItems && toIndex <= totalItems && fromIndex <= toIndex) {
                    List<AdComponent> pageData = adsObservableList.subList(fromIndex, toIndex);
                    adListView.setItems(FXCollections.observableArrayList(pageData));
                } else if (fromIndex >= totalItems && totalItems > 0) {
                    currentPage = totalPages;
                    int adjustedFromIndex = (currentPage - 1) * pageSize;
                    int adjustedToIndex = Math.min(adjustedFromIndex + pageSize, totalItems);
                    if (adjustedFromIndex < adjustedToIndex) {
                        List<AdComponent> pageData = adsObservableList.subList(adjustedFromIndex, adjustedToIndex);
                        adListView.setItems(FXCollections.observableArrayList(pageData));
                    } else {
                        adListView.setItems(FXCollections.emptyObservableList());
                    }
                } else if (fromIndex >= toIndex) {
                    adListView.setItems(FXCollections.emptyObservableList());
                } else {
                    LOGGER.severe("Pagination error (unexpected state): fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", totalItems=" + totalItems + ", currentPage=" + currentPage);
                    adListView.setItems(FXCollections.emptyObservableList());
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
        String maxPriceText = (maxPriceField != null) ?
                maxPriceField.getText() : "";
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
            totalAdsLabel.setText("Всього (фільтр.): " + (adsObservableList != null ? adsObservableList.size() : 0));
        }
        if (filteredAdsLabel != null && adListView != null && adListView.getItems() != null) {
            filteredAdsLabel.setText("На сторінці: " + adListView.getItems().size());
        }
        if (selectedCategoryLabel != null) {
            if (currentSelectedCategoryId == null) {
                selectedCategoryLabel.setText("Обрана категорія: Всі");
            } else if (categoryTreeView != null && categoryTreeView.getSelectionModel().getSelectedItem() != null) {
                CategoryComponent selectedComp = categoryTreeView.getSelectionModel().getSelectedItem().getValue();
                if (selectedComp != null) {
                    selectedCategoryLabel.setText("Обрана категорія: " + selectedComp.getName());
                }
            } else if (currentCategoryLabel != null && !currentCategoryLabel.getText().equals("Всі оголошення")) {
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
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
            }
            if (loadingLabel != null) {
                loadingLabel.setText("");
            }
        });
    }

    private void setupCategoryTree() {
        if (categoryTreeView == null || categoryService == null) {
            LOGGER.severe("Error: categoryTreeView or categoryService is null. Cannot setup category tree.");
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
                LOGGER.warning("Warning: CategoryService.getAllRootCategories() returned null. Using empty list.");
            }

            Category allCategoriesDataNode = new Category("root", "Всі категорії", null);
            TreeItem<CategoryComponent> rootTreeItem = new TreeItem<>(allCategoriesDataNode);
            rootTreeItem.setExpanded(true);

            for (CategoryComponent rootCategory : rootCategories) {
                if (rootCategory != null) {
                    TreeItem<CategoryComponent> categoryItem = createTreeItem(rootCategory, true);
                    if (categoryItem != null) {
                        rootTreeItem.getChildren().add(categoryItem);
                    }
                } else {
                    LOGGER.warning("Warning: Found null root category, skipping...");
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
            LOGGER.log(Level.SEVERE, "Error setting up category tree", e);
            Category fallbackRootData = new Category("error_root", "Помилка завантаження", null);
            TreeItem<CategoryComponent> fallbackItem = new TreeItem<>(fallbackRootData);
            fallbackItem.setExpanded(true);
            if (categoryTreeView != null) {
                categoryTreeView.setRoot(fallbackItem);
                categoryTreeView.setShowRoot(true);
            }
        }
    }

    private TreeItem<CategoryComponent> createTreeItem(CategoryComponent categoryComponent, boolean autoExpand) {
        if (categoryComponent == null) {
            LOGGER.warning("Warning: Attempting to create TreeItem for null categoryComponent, skipping.");
            return null;
        }

        TreeItem<CategoryComponent> item = new TreeItem<>(categoryComponent);
        item.setExpanded(autoExpand);
        if (categoryComponent instanceof Category) {
            Category category = (Category) categoryComponent;
            CategoryComponent[] childrenArray = category.getChildren();

            if (childrenArray != null) {
                for (CategoryComponent childComp : childrenArray) {
                    if (childComp != null) {
                        TreeItem<CategoryComponent> childItem = createTreeItem(childComp, false);
                        if (childItem != null) {
                            item.getChildren().add(childItem);
                        }
                    } else {
                        LOGGER.warning("Warning: Found null child category in category: " + (category.getName() != null ? category.getName() : "ID: " + category.getId()) + ", skipping child.");
                    }
                }
            }
        }
        return item;
    }

    private void loadAds(String categoryId) {
        LOGGER.info("Loading ads for category: " + (categoryId == null ? "All" : categoryId));
        if (adService == null) {
            showErrorAlert("Помилка", "Сервіс оголошень недоступний.", "Неможливо завантажити оголошення.");
            hideLoadingIndicator();
            return;
        }
        showLoadingIndicator("Завантаження оголошень...");
        String keyword = (searchField != null) ?
                searchField.getText() : "";

        Task<List<AdComponent>> loadTask = new Task<>() {
            @Override
            protected List<AdComponent> call() throws Exception {
                List<Ad> ads = adService.searchAds(keyword, null, null, categoryId);
                List<Ad> filteredByQuickFilters = new ArrayList<>();
                if (ads != null) {
                    for (Ad ad : ads) {
                        if (ad == null) continue;
                        filteredByQuickFilters.add(ad);
                    }
                } else {
                    ads = new ArrayList<>();
                }

                LOGGER.info("Total ads fetched by service: " + (ads != null ? ads.size() : 0));
                LOGGER.info("Ads after (removed) quick filtering: " + filteredByQuickFilters.size());

                return filteredByQuickFilters.stream()
                        .map(MainController.this::createDecoratedAd)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        };
        loadTask.setOnSucceeded(event -> {
            List<AdComponent> decoratedAds = loadTask.getValue();
            LOGGER.info("Decorated ads count for UI update: " + decoratedAds.size());
            adsObservableList.setAll(decoratedAds);
            applySorting();
            updatePaginationControls();
            updateActiveFiltersDisplay();
            updateStatistics();
            hideLoadingIndicator();
            updateStatus("Завантажено " + adsObservableList.size() + " відповідних оголошень (до пагінації). На сторінці: " + (adListView != null && adListView.getItems() != null ? adListView.getItems().size() : 0) );
        });
        loadTask.setOnFailed(event -> {
            LOGGER.log(Level.SEVERE, "Failed to load ads", loadTask.getException());
            hideLoadingIndicator();
            showErrorAlert("Помилка завантаження", "Не вдалося завантажити оголошення.", loadTask.getException().getMessage());
            adsObservableList.clear();
            updatePaginationControls();
            updateStatistics();
        });
        new Thread(loadTask).start();
    }


    private void refreshCurrentView() {
        boolean advancedFiltersActive = (minPriceField != null && !minPriceField.getText().isEmpty()) ||
                (maxPriceField != null && !maxPriceField.getText().isEmpty()) ||
                (statusFilterCombo != null && statusFilterCombo.getValue() != null && !"Всі".equals(statusFilterCombo.getValue())) ||
                (premiumOnlyCheckBox != null && premiumOnlyCheckBox.isSelected()) ||
                (urgentOnlyCheckBox != null && urgentOnlyCheckBox.isSelected());
        if (advancedFiltersActive) {
            handleApplyFilters();
        } else {
            loadAds(currentSelectedCategoryId);
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
    public void handleCreateAd() {
        try {
            MainGuiApp.loadCreateAdScene();
            refreshCurrentView();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open create ad form", e);
            showErrorAlert("Помилка", "Не вдалося відкрити форму створення оголошення", e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        try {
            MainGuiApp.loadLoginScene();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to login screen", e);
            showErrorAlert("Помилка", "Не вдалося перейти до екрану входу", e.getMessage());
        }
    }

    @FXML
    public void handleExitApplication() {
        try {
            if (MainGuiApp.sessionManager != null) {
                MainGuiApp.sessionManager.saveState();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving session state on exit", e);
        } finally {
            Platform.exit();
            System.exit(0);
        }
    }

    private void showErrorAlert(String title, String message) {
        showErrorAlert(title, null, message);
    }

    private void showErrorAlert(String title, String header, String content) {
        if (title == null) {
            title = "Помилка";
        }
        if (header == null) {
            header = "Виникла помилка";
        }
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
            try {
                URL cssUrl = getClass().getResource("/styles/alert-styles.css");
                if (cssUrl != null) {
                    alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    LOGGER.warning("Alert CSS /styles/alert-styles.css not found.");
                }
            } catch (Exception e)
            {
                LOGGER.log(Level.WARNING, "Failed to load alert styles", e);
            }
            alert.showAndWait();
        });
    }

    @Contract(pure = true)
    private void showError(String message) {
        if (message == null || message.trim().isEmpty()) {
            LOGGER.severe("showError called with empty message");
            return;
        }
        LOGGER.severe("ПОМИЛКА: " + message);
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
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
        Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
        yesButton.setText("Так");
        noButton.setText("Ні");
        try {
            URL cssUrl = getClass().getResource("/styles/alert-styles.css");
            if (cssUrl != null) {
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            } else {
                LOGGER.warning("Alert CSS /styles/alert-styles.css not found.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load alert styles for confirmation", e);
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

            Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("Гаразд");

            try {
                URL cssUrl = getClass().getResource("/styles/alert-styles.css");
                if (cssUrl != null) {
                    alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    LOGGER.warning("Alert CSS /styles/alert-styles.css not found.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load alert styles for info", e);
            }
            alert.showAndWait();
        });
    }


    private AdComponent createDecoratedAd(Ad ad) {
        if (ad == null) {
            LOGGER.warning("Attempted to decorate null ad");
            return null;
        }
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

    public void updateAdsList(List<Ad> adsFromMediator) {
        LOGGER.info("Received ads from mediator: " + (adsFromMediator != null ? adsFromMediator.size() : "null"));
        if (adsFromMediator == null) {
            adsFromMediator = new ArrayList<>();
        }

        List<AdComponent> decoratedAds = adsFromMediator.stream()
                .map(this::createDecoratedAd)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        LOGGER.info("Decorated ads count from mediator: " + decoratedAds.size());

        Platform.runLater(() -> {
            LOGGER.info("Updating UI with " + decoratedAds.size() + " ads from mediator");
            adsObservableList.setAll(decoratedAds);
            applySorting();
            updatePaginationControls();
            updateActiveFiltersDisplay();
            updateStatus("Оновлено " + decoratedAds.size() + " оголошень (медіатор)");
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

    public void cleanup() {
        updateStatus("Очищення контролера...");
        if (mediator != null) {
            updateMediatorStatus("неактивний (очищено)");
        }
        LOGGER.info("MainController cleanup finished.");
    }
}
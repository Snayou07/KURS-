package com.example.olx.presentation.gui.mediator;

import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.application.service.port.CategoryServicePort;
import com.example.olx.domain.model.Ad;
import com.example.olx.presentation.gui.mediator.components.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * Конкретний медіатор для координації взаємодії компонентів перегляду оголошень
 */
public class AdBrowserMediator implements UIMediator {
    private final AdServicePort adService;
    private final CategoryServicePort categoryService;

    private SearchComponent searchComponent;
    private AdListComponent adListComponent;
    private FilterComponent filterComponent;

    // Флаг для перевірки ініціалізації компонентів
    private boolean componentsInitialized = false;

    public AdBrowserMediator(AdServicePort adService, CategoryServicePort categoryService) {
        this.adService = adService;
        this.categoryService = categoryService;
    }

    public void registerComponents(SearchComponent searchComponent,
                                   AdListComponent adListComponent,
                                   FilterComponent filterComponent) {
        this.searchComponent = searchComponent;
        this.adListComponent = adListComponent;
        this.filterComponent = filterComponent;

        // Встановлюємо медіатор для всіх компонентів
        if (searchComponent != null) searchComponent.setMediator(this);
        if (adListComponent != null) adListComponent.setMediator(this);
        if (filterComponent != null) filterComponent.setMediator(this);

        this.componentsInitialized = true;

        // Завантажуємо початкові дані
        loadAllAds();
    }

    @Override
    public void notify(Object sender, String event, Object data) {
        if (!componentsInitialized) {
            System.out.println("Компоненти ще не ініціалізовані, пропускаємо подію: " + event);
            return;
        }

        System.out.println("Медіатор отримав подію '" + event + "' від " +
                (sender != null ? sender.getClass().getSimpleName() : "невідомого відправника"));

        try {
            switch (event) {
                case "searchTextChanged":
                    handleSearchTextChanged((String) data);
                    break;
                case "categoryChanged":
                    handleCategoryChanged((String) data);
                    break;
                case "priceRangeChanged":
                    handlePriceRangeChanged((double[]) data);
                    break;
                case "searchRequested":
                    handleSearchRequested((SearchComponent.SearchCriteria) data);
                    break;
                case "searchCleared":
                    handleSearchCleared();
                    break;
                case "adSelected":
                    handleAdSelected((Ad) data);
                    break;
                case "adDetailsRequested":
                    handleAdDetailsRequested((String) data);
                    break;
                case "addToFavorites":
                    handleAddToFavorites((String) data);
                    break;
                case "activeFilterToggled":
                    handleActiveFilterToggled((Boolean) data);
                    break;
                case "sortCriteriaChanged":
                    handleSortCriteriaChanged((FilterComponent.SortCriteria) data);
                    break;
                case "filtersApplied":
                    handleFiltersApplied((FilterComponent.FilterCriteria) data);
                    break;
                case "filtersReset":
                    handleFiltersReset();
                    break;
                case "adCreated":
                    handleAdCreated((Ad) data);
                    break;
                case "adUpdated":
                    handleAdUpdated((Ad) data);
                    break;
                case "adDeleted":
                    handleAdDeleted((String) data);
                    break;
                case "refreshRequested":
                    handleRefreshRequested();
                    break;
                default:
                    System.out.println("Невідома подія: " + event);
            }
        } catch (Exception e) {
            System.err.println("Помилка при обробці події '" + event + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSearchTextChanged(String text) {
        System.out.println("Текст пошуку змінено на: " + text);
        // Можна додати автокомпліт або живий пошук
    }

    private void handleCategoryChanged(String categoryId) {
        System.out.println("Категорія змінена на: " + categoryId);
        // Можна оновити фільтри відповідно до категорії
    }

    private void handlePriceRangeChanged(double[] priceRange) {
        System.out.println("Діапазон цін змінено: " + priceRange[0] + " - " + priceRange[1]);
    }

    private void handleSearchRequested(SearchComponent.SearchCriteria criteria) {
        System.out.println("Виконується пошук за критеріями...");

        try {
            List<Ad> results = adService.searchAds(
                    criteria.getKeyword(),
                    criteria.getMinPrice(),
                    criteria.getMaxPrice(),
                    criteria.getCategoryId()
            );

            // Застосовуємо фільтри
            results = applyCurrentFilters(results);

            if (adListComponent != null) {
                adListComponent.updateAdList(results);
            }
        } catch (Exception e) {
            System.err.println("Помилка при пошуку оголошень: " + e.getMessage());
            if (adListComponent != null) {
                adListComponent.showNoResults();
            }
        }
    }

    private void handleSearchCleared() {
        System.out.println("Пошук очищено");
        loadAllAds();
    }

    private void handleAdSelected(Ad ad) {
        System.out.println("Вибрано оголошення: " + ad.getTitle());
        // Можна показати деталі або виконати інші дії
    }

    private void handleAdDetailsRequested(String adId) {
        System.out.println("Запитано деталі оголошення: " + adId);
        try {
            adService.getAdById(adId).ifPresentOrElse(
                    ad -> System.out.println("Деталі: " + ad.toString()),
                    () -> System.out.println("Оголошення не знайдено")
            );
        } catch (Exception e) {
            System.err.println("Помилка при отриманні деталей оголошення: " + e.getMessage());
        }
    }

    private void handleAddToFavorites(String adId) {
        System.out.println("Додано до обраних: " + adId);
        // Тут би була логіка додавання до обраних
    }

    private void handleActiveFilterToggled(Boolean showOnlyActive) {
        System.out.println("Фільтр активних оголошень: " + (showOnlyActive ? "увімкнено" : "вимкнено"));
        refreshAdList();
    }

    private void handleSortCriteriaChanged(FilterComponent.SortCriteria criteria) {
        System.out.println("Критерії сортування змінено: " + criteria.getSortBy() +
                " (" + (criteria.isAscending() ? "зростання" : "спадання") + ")");
        refreshAdList();
    }

    private void handleFiltersApplied(FilterComponent.FilterCriteria criteria) {
        System.out.println("Застосовано фільтри");
        refreshAdList();
    }

    private void handleFiltersReset() {
        System.out.println("Фільтри скинуто");
        refreshAdList();
    }

    // Нові методи для обробки CRUD операцій з оголошеннями
    private void handleAdCreated(Ad newAd) {
        System.out.println("Створено нове оголошення: " + newAd.getTitle());
        refreshAdList();
    }

    private void handleAdUpdated(Ad updatedAd) {
        System.out.println("Оновлено оголошення: " + updatedAd.getTitle());
        refreshAdList();
    }

    private void handleAdDeleted(String adId) {
        System.out.println("Видалено оголошення з ID: " + adId);
        refreshAdList();
    }

    private void handleRefreshRequested() {
        System.out.println("Запитано оновлення списку оголошень");
        loadAllAds();
    }

    private void refreshAdList() {
        if (!componentsInitialized || searchComponent == null || adListComponent == null) {
            System.out.println("Компоненти не ініціалізовані, пропускаємо оновлення списку");
            return;
        }

        try {
            // Отримуємо поточні результати пошуку або всі оголошення
            List<Ad> ads;
            if (isSearchEmpty()) {
                ads = adService.getAllAds();
            } else {
                ads = adService.searchAds(
                        searchComponent.getSearchText(),
                        searchComponent.getMinPrice(),
                        searchComponent.getMaxPrice(),
                        searchComponent.getSelectedCategory()
                );
            }

            ads = applyCurrentFilters(ads);
            adListComponent.updateAdList(ads);
        } catch (Exception e) {
            System.err.println("Помилка при оновленні списку оголошень: " + e.getMessage());
            if (adListComponent != null) {
                adListComponent.showNoResults();
            }
        }
    }

    private boolean isSearchEmpty() {
        return (searchComponent.getSearchText() == null || searchComponent.getSearchText().trim().isEmpty()) &&
                (searchComponent.getSelectedCategory() == null || searchComponent.getSelectedCategory().isEmpty()) &&
                searchComponent.getMinPrice() == null &&
                searchComponent.getMaxPrice() == null;
    }

    private List<Ad> applyCurrentFilters(List<Ad> ads) {
        if (ads == null || ads.isEmpty() || filterComponent == null) {
            return ads != null ? ads : List.of();
        }

        try {
            List<Ad> filtered = ads;

            // Фільтр активних оголошень
            if (filterComponent.isShowOnlyActive()) {
                filtered = filtered.stream()
                        .filter(ad -> "Активне".equals(ad.getStatus()))
                        .collect(Collectors.toList());
            }

            // Сортування
            String sortBy = filterComponent.getSortBy();
            if (sortBy != null) {
                switch (sortBy) {
                    case "price":
                        filtered = filtered.stream()
                                .sorted(filterComponent.isSortAscending() ?
                                        Comparator.comparing(Ad::getPrice, Comparator.nullsLast(Comparator.naturalOrder())) :
                                        Comparator.comparing(Ad::getPrice, Comparator.nullsLast(Comparator.reverseOrder())))
                                .collect(Collectors.toList());
                        break;
                    case "title":
                        filtered = filtered.stream()
                                .sorted(filterComponent.isSortAscending() ?
                                        Comparator.comparing(Ad::getTitle, Comparator.nullsLast(Comparator.naturalOrder())) :
                                        Comparator.comparing(Ad::getTitle, Comparator.nullsLast(Comparator.reverseOrder())))
                                .collect(Collectors.toList());
                        break;
                    // За замовчуванням сортування за датою (якщо є поле дати)
                    default:
                        // Можна додати сортування за датою, якщо є відповідне поле в Ad
                        break;
                }
            }

            return filtered;
        } catch (Exception e) {
            System.err.println("Помилка при застосуванні фільтрів: " + e.getMessage());
            return ads;
        }
    }

    // Публічні методи для ініціалізації та управління
    public void loadAllAds() {
        if (!componentsInitialized || adListComponent == null) {
            System.out.println("Компоненти не ініціалізовані, пропускаємо завантаження оголошень");
            return;
        }

        try {
            List<Ad> allAds = adService.getAllAds();
            allAds = applyCurrentFilters(allAds);
            adListComponent.updateAdList(allAds);
            System.out.println("Завантажено " + allAds.size() + " оголошень");
        } catch (Exception e) {
            System.err.println("Помилка при завантаженні оголошень: " + e.getMessage());
            if (adListComponent != null) {
                adListComponent.showNoResults();
            }
        }
    }

    // Метод для повідомлення медіатора про створення нового оголошення
    public void notifyAdCreated(Ad newAd) {
        notify(this, "adCreated", newAd);
    }

    // Метод для повідомлення медіатора про оновлення оголошення
    public void notifyAdUpdated(Ad updatedAd) {
        notify(this, "adUpdated", updatedAd);
    }

    // Метод для повідомлення медіатора про видалення оголошення
    public void notifyAdDeleted(String adId) {
        notify(this, "adDeleted", adId);
    }

    // Метод для ручного оновлення списку
    public void refresh() {
        notify(this, "refreshRequested", null);
    }

    // Геттери для перевірки стану
    public boolean isInitialized() {
        return componentsInitialized;
    }

    public AdListComponent getAdListComponent() {
        return adListComponent;
    }

    public SearchComponent getSearchComponent() {
        return searchComponent;
    }

    public FilterComponent getFilterComponent() {
        return filterComponent;
    }
}
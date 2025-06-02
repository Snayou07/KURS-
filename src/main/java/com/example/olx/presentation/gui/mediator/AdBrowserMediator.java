package com.example.olx.presentation.gui.mediator;

import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.application.service.port.CategoryServicePort;
import com.example.olx.domain.model.Ad;
import com.example.olx.presentation.gui.controller.MainController;
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
    private MainController controller;

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
        searchComponent.setMediator(this);
        adListComponent.setMediator(this);
        filterComponent.setMediator(this);
    }

    @Override
    public void notify(Object sender, String event, Object data) {
        System.out.println("Медіатор отримав подію '" + event + "' від " + sender.getClass().getSimpleName());

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
            default:
                System.out.println("Невідома подія: " + event);
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

        List<Ad> results = adService.searchAds(
                criteria.getKeyword(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getCategoryId()
        );

        // Застосовуємо фільтри
        results = applyCurrentFilters(results);

        adListComponent.updateAdList(results);
    }

    private void handleSearchCleared() {
        System.out.println("Пошук очищено");
        List<Ad> allAds = adService.getAllAds();
        allAds = applyCurrentFilters(allAds);
        adListComponent.updateAdList(allAds);
    }

    private void handleAdSelected(Ad ad) {
        System.out.println("Вибрано оголошення: " + ad.getTitle());
        // Можна показати деталі або виконати інші дії
    }

    private void handleAdDetailsRequested(String adId) {
        System.out.println("Запитано деталі оголошення: " + adId);
        adService.getAdById(adId).ifPresentOrElse(
                ad -> System.out.println("Деталі: " + ad.toString()),
                () -> System.out.println("Оголошення не знайдено")
        );
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

    private void refreshAdList() {
        // Отримуємо поточні результати пошуку або всі оголошення
        List<Ad> ads;
        if (searchComponent.getSearchText().isEmpty() &&
                searchComponent.getSelectedCategory().isEmpty() &&
                searchComponent.getMinPrice() == null &&
                searchComponent.getMaxPrice() == null) {
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
    }

    private List<Ad> applyCurrentFilters(List<Ad> ads) {
        List<Ad> filtered = ads;

        // Фільтр активних оголошень
        if (filterComponent.isShowOnlyActive()) {
            filtered = filtered.stream()
                    .filter(ad -> "Активне".equals(ad.getStatus()))
                    .collect(Collectors.toList());
        }

        // Сортування
        switch (filterComponent.getSortBy()) {
            case "price":
                filtered = filtered.stream()
                        .sorted(filterComponent.isSortAscending() ?
                                Comparator.comparing(Ad::getPrice) :
                                Comparator.comparing(Ad::getPrice).reversed())
                        .collect(Collectors.toList());
                break;
            case "title":
                filtered = filtered.stream()
                        .sorted(filterComponent.isSortAscending() ?
                                Comparator.comparing(Ad::getTitle) :
                                Comparator.comparing(Ad::getTitle).reversed())
                        .collect(Collectors.toList());
                break;
            // За замовчуванням сортування за датою (тут би використовувалася дата створення)
        }

        return filtered;
    }

    // Публічні методи для ініціалізації
    public void loadAllAds() {
        List<Ad> allAds = adService.getAllAds();
        allAds = applyCurrentFilters(allAds);
        adListComponent.updateAdList(allAds);
    }

    public void setController(MainController controller) {
        this.controller = controller;
    }
}
// src/main/java/com/example/olx/presentation/mediator/components/SearchComponent.java
package com.example.olx.presentation.gui.mediator.components;

import com.example.olx.presentation.gui.mediator.UIComponent;
import com.example.olx.presentation.gui.mediator.UIMediator;

/**
 * Компонент пошуку оголошень
 */
public class SearchComponent extends UIComponent {
    private String searchText = "";
    private String selectedCategory = "";
    private Double minPrice = null;
    private Double maxPrice = null;

    public SearchComponent(UIMediator mediator) {
        super(mediator);
    }

    public void updateSearchText(String text) {
        this.searchText = text;
        mediator.notify(this, "searchTextChanged", text);
    }

    public void updateCategory(String categoryId) {
        this.selectedCategory = categoryId;
        mediator.notify(this, "categoryChanged", categoryId);
    }

    public void updatePriceRange(Double minPrice, Double maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        mediator.notify(this, "priceRangeChanged", new double[]{minPrice != null ? minPrice : 0, maxPrice != null ? maxPrice : Double.MAX_VALUE});
    }

    public void performSearch() {
        SearchCriteria criteria = new SearchCriteria(searchText, minPrice, maxPrice, selectedCategory);
        mediator.notify(this, "searchRequested", criteria);
    }

    public void clearSearch() {
        this.searchText = "";
        this.selectedCategory = "";
        this.minPrice = null;
        this.maxPrice = null;
        mediator.notify(this, "searchCleared", null);
    }

    // Геттери
    public String getSearchText() { return searchText; }
    public String getSelectedCategory() { return selectedCategory; }
    public Double getMinPrice() { return minPrice; }
    public Double getMaxPrice() { return maxPrice; }

    // Клас для передачі критеріїв пошуку
    public static class SearchCriteria {
        private final String keyword;
        private final Double minPrice;
        private final Double maxPrice;
        private final String categoryId;

        public SearchCriteria(String keyword, Double minPrice, Double maxPrice, String categoryId) {
            this.keyword = keyword;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.categoryId = categoryId;
        }

        public String getKeyword() { return keyword; }
        public Double getMinPrice() { return minPrice; }
        public Double getMaxPrice() { return maxPrice; }
        public String getCategoryId() { return categoryId; }
    }
}
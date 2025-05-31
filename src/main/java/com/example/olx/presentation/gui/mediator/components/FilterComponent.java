package com.example.olx.presentation.gui.mediator.components;

import com.example.olx.presentation.gui.mediator.UIComponent;
import com.example.olx.presentation.gui.mediator.UIMediator;

/**
 * Компонент фільтрації оголошень
 */
public class FilterComponent extends UIComponent {
    private boolean showOnlyActive = true;
    private String sortBy = "date"; // date, price, title
    private boolean sortAscending = false;

    public FilterComponent(UIMediator mediator) {
        super(mediator);
    }

    public void toggleActiveOnly() {
        this.showOnlyActive = !showOnlyActive;
        mediator.notify(this, "activeFilterToggled", showOnlyActive);
    }

    public void setSortCriteria(String sortBy, boolean ascending) {
        this.sortBy = sortBy;
        this.sortAscending = ascending;
        SortCriteria criteria = new SortCriteria(sortBy, ascending);
        mediator.notify(this, "sortCriteriaChanged", criteria);
    }

    public void applyFilters() {
        FilterCriteria criteria = new FilterCriteria(showOnlyActive, sortBy, sortAscending);
        mediator.notify(this, "filtersApplied", criteria);
    }

    public void resetFilters() {
        this.showOnlyActive = true;
        this.sortBy = "date";
        this.sortAscending = false;
        mediator.notify(this, "filtersReset", null);
    }

    // Геттери
    public boolean isShowOnlyActive() { return showOnlyActive; }
    public String getSortBy() { return sortBy; }
    public boolean isSortAscending() { return sortAscending; }

    // Допоміжні класи
    public static class FilterCriteria {
        private final boolean showOnlyActive;
        private final String sortBy;
        private final boolean sortAscending;

        public FilterCriteria(boolean showOnlyActive, String sortBy, boolean sortAscending) {
            this.showOnlyActive = showOnlyActive;
            this.sortBy = sortBy;
            this.sortAscending = sortAscending;
        }

        public boolean isShowOnlyActive() { return showOnlyActive; }
        public String getSortBy() { return sortBy; }
        public boolean isSortAscending() { return sortAscending; }
    }

    public static class SortCriteria {
        private final String sortBy;
        private final boolean ascending;

        public SortCriteria(String sortBy, boolean ascending) {
            this.sortBy = sortBy;
            this.ascending = ascending;
        }

        public String getSortBy() { return sortBy; }
        public boolean isAscending() { return ascending; }
    }
}
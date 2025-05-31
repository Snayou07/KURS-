package com.example.olx.presentation.gui.mediator.components;

import com.example.olx.domain.model.Ad;
import com.example.olx.presentation.gui.mediator.UIComponent;
import com.example.olx.presentation.gui.mediator.UIMediator;
import java.util.List;
import java.util.ArrayList;

/**
 * Компонент для відображення списку оголошень
 */
public class AdListComponent extends UIComponent {
    private List<Ad> currentAds = new ArrayList<>();
    private Ad selectedAd = null;

    public AdListComponent(UIMediator mediator) {
        super(mediator);
    }

    public void updateAdList(List<Ad> ads) {
        this.currentAds = new ArrayList<>(ads);
        mediator.notify(this, "adListUpdated", ads.size());
        System.out.println("Список оголошень оновлено. Знайдено: " + ads.size() + " оголошень");
    }

    public void selectAd(String adId) {
        this.selectedAd = currentAds.stream()
                .filter(ad -> ad.getAdId().equals(adId))
                .findFirst()
                .orElse(null);

        if (selectedAd != null) {
            mediator.notify(this, "adSelected", selectedAd);
        }
    }

    public void requestAdDetails(String adId) {
        mediator.notify(this, "adDetailsRequested", adId);
    }

    public void addToFavorites(String adId) {
        mediator.notify(this, "addToFavorites", adId);
    }

    public void showNoResults() {
        this.currentAds.clear();
        System.out.println("Оголошення за заданими критеріями не знайдені");
        mediator.notify(this, "noResultsFound", null);
    }

    // Геттери
    public List<Ad> getCurrentAds() { return new ArrayList<>(currentAds); }
    public Ad getSelectedAd() { return selectedAd; }
}
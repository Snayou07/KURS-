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
        this.currentAds = new ArrayList<>(ads != null ? ads : new ArrayList<>());

        // Повідомляємо медіатор тільки якщо він встановлений
        if (mediator != null) {
            mediator.notify(this, "adListUpdated", currentAds.size());
        }

        System.out.println("Список оголошень оновлено. Знайдено: " + currentAds.size() + " оголошень");
    }

    public void selectAd(String adId) {
        if (adId == null || currentAds.isEmpty()) {
            return;
        }

        this.selectedAd = currentAds.stream()
                .filter(ad -> ad != null && adId.equals(ad.getAdId()))
                .findFirst()
                .orElse(null);

        if (selectedAd != null && mediator != null) {
            mediator.notify(this, "adSelected", selectedAd);
        }
    }

    public void requestAdDetails(String adId) {
        if (adId != null && mediator != null) {
            mediator.notify(this, "adDetailsRequested", adId);
        }
    }

    public void addToFavorites(String adId) {
        if (adId != null && mediator != null) {
            mediator.notify(this, "addToFavorites", adId);
        }
    }

    public void showNoResults() {
        this.currentAds.clear();
        System.out.println("Оголошення за заданими критеріями не знайдені");

        if (mediator != null) {
            mediator.notify(this, "noResultsFound", null);
        }
    }

    // Метод для безпечного оновлення без повідомлення медіатора
    public void updateAdListSilently(List<Ad> ads) {
        this.currentAds = new ArrayList<>(ads != null ? ads : new ArrayList<>());
        System.out.println("Список оголошень оновлено тихо. Знайдено: " + currentAds.size() + " оголошень");
    }

    // Перевірка наявності оголошень
    public boolean hasAds() {
        return !currentAds.isEmpty();
    }

    // Отримання кількості оголошень
    public int getAdsCount() {
        return currentAds.size();
    }

    // Геттери
    public List<Ad> getCurrentAds() {
        return new ArrayList<>(currentAds);
    }

    public Ad getSelectedAd() {
        return selectedAd;
    }

    // Метод для відображення списку в консолі (для відладки)
    public void printCurrentAds() {
        System.out.println("=== Поточний список оголошень ===");
        if (currentAds.isEmpty()) {
            System.out.println("Список порожній");
        } else {
            for (int i = 0; i < currentAds.size(); i++) {
                Ad ad = currentAds.get(i);
                System.out.printf("%d. %s - %s грн (ID: %s)%n",
                        i + 1,
                        ad.getTitle(),
                        ad.getPrice(),
                        ad.getAdId());
            }
        }
        System.out.println("=== Кінець списку ===");
    }
}
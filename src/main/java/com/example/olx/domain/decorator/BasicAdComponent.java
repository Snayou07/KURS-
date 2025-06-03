package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

public class BasicAdComponent implements AdComponent {
    private final Ad ad;

    public BasicAdComponent(Ad ad) {
        if (ad == null) {
            throw new IllegalArgumentException("Ad cannot be null in BasicAdComponent");
        }
        this.ad = ad;
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Назва: %s\nОпис: %s\nЦіна: %.2f грн\nСтатус: %s",
                ad.getTitle() != null ? ad.getTitle() : "Без назви",
                ad.getDescription() != null ? ad.getDescription() : "Без опису",
                ad.getPrice(),
                ad.getState() != null ? ad.getState() : "Невідомий");
    }

    @Override
    public double getCalculatedPrice() {
        return ad.getPrice();
    }

    @Override
    public String getFormattedTitle() {
        return ad.getTitle() != null ? ad.getTitle() : "Без назви";
    }

    @Override
    public Ad getAd() {
        return ad;
    }

    // Додатковий метод для перевірки валідності
    public boolean isValid() {
        return ad != null && ad.getTitle() != null && ad.getPrice() >= 0;
    }
}
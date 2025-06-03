package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

public class BasicAdComponent implements AdComponent {
    private final Ad ad;

    public BasicAdComponent(Ad ad) {
        this.ad = ad;
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Назва: %s\nОпис: %s\nЦіна: %.2f грн\nСтатус: %s",
                ad.getTitle(),
                ad.getDescription() != null ? ad.getDescription() : "Без опису",
                ad.getPrice(),
                ad.getState()); // Fixed: Changed from getStatus() to getState()
    }

    @Override
    public double getCalculatedPrice() {
        return ad.getPrice();
    }

    @Override
    public String getFormattedTitle() {
        return ad.getTitle();
    }

    @Override
    public Ad getAd() {
        return ad;
    }
}
// src/main/java/com/example/olx/domain/decorator/BasicAdComponent.java
package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

public class BasicAdComponent implements AdComponent {
    protected Ad ad;

    public BasicAdComponent(Ad ad) {
        if (ad == null) {
            throw new IllegalArgumentException("Ad cannot be null");
        }
        this.ad = ad;
    }

    @Override
    public String getTitle() {
        return ad.getTitle();
    }

    @Override
    public String getDescription() {
        return ad.getDescription();
    }

    @Override
    public double getPrice() {
        return ad.getPrice();
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Назва: %s\nОпис: %s\nЦіна: %.2f грн",
                getTitle(), getDescription(), getPrice());
    }

    @Override
    public Ad getAd() {
        return ad;
    }
}
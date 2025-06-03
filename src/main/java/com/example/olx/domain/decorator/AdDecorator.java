package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

public abstract class AdDecorator implements AdComponent {
    protected AdComponent component;

    public AdDecorator(AdComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null in AdDecorator");
        }
        this.component = component;
    }

    @Override
    public String getDisplayInfo() {
        return component.getDisplayInfo();
    }

    @Override
    public double getCalculatedPrice() {
        return component.getCalculatedPrice();
    }

    @Override
    public String getFormattedTitle() {
        return component.getFormattedTitle();
    }

    @Override
    public Ad getAd() {
        // Важливо: переконуємося, що завжди повертаємо оригінальний Ad
        return component.getAd();
    }

    // Додатковий helper метод для отримання базового Ad без декораторів
    protected Ad getBaseAd() {
        AdComponent current = this.component;
        while (current instanceof AdDecorator) {
            current = ((AdDecorator) current).component;
        }
        return current.getAd();
    }
}
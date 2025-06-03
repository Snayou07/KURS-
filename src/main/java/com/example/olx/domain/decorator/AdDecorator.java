// src/main/java/com/example/olx/domain/decorator/AdDecorator.java
package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

public abstract class AdDecorator implements AdComponent {
    protected AdComponent component;

    public AdDecorator(AdComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }
        this.component = component;
    }
    @Override
    public String getFormattedTitle() {
        return component.getFormattedTitle();
    }

    @Override
    public double getCalculatedPrice() {
        return component.getCalculatedPrice();
    }
    @Override
    public String getTitle() {
        return component.getTitle();
    }

    @Override
    public String getDescription() {
        return component.getDescription();
    }

    @Override
    public double getPrice() {
        return component.getPrice();
    }

    @Override
    public String getDisplayInfo() {
        return component.getDisplayInfo();
    }

    @Override
    public Ad getAd() {
        return component.getAd();
    }
}
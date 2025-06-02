package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

public abstract class AdDecorator implements AdComponent {
    protected AdComponent component;

    public AdDecorator(AdComponent component) {
        this.component = component != null ? component : new BasicAdComponent(null);
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
        return component.getAd();
    }
}
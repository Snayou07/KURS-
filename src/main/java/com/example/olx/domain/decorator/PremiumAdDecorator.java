package com.example.olx.domain.decorator;

public class PremiumAdDecorator extends AdDecorator {

    public PremiumAdDecorator(AdComponent component) {
        super(component);
    }

    @Override
    public String getDisplayInfo() {
        return "⭐ ПРЕМІУМ ⭐\n" + super.getDisplayInfo();
    }

    @Override
    public String getFormattedTitle() {
        return "⭐ " + super.getFormattedTitle() + " [ПРЕМІУМ]";
    }
}
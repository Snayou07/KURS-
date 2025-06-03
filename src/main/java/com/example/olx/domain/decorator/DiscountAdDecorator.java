// src/main/java/com/example/olx/domain/decorator/DiscountAdDecorator.java
package com.example.olx.domain.decorator;

public class DiscountAdDecorator extends AdDecorator {
    private double discountPercentage;
    private String reason;

    public DiscountAdDecorator(AdComponent component, double discountPercentage, String reason) {
        super(component);
        this.discountPercentage = discountPercentage;
        this.reason = reason != null ? reason : "Спеціальна пропозиція";
    }

    @Override
    public String getTitle() {
        return String.format("💰 ЗНИЖКА %.0f%% - %s", discountPercentage, super.getTitle());
    }

    @Override
    public double getPrice() {
        double originalPrice = super.getPrice();
        return originalPrice * (1 - discountPercentage / 100);
    }
    @Override
    public String getFormattedTitle() {
        return String.format("💰 ЗНИЖКА %.0f%% - %s", discountPercentage, super.getFormattedTitle());
    }

    @Override
    public double getCalculatedPrice() {
        return getPrice(); // вже враховує знижку
    }
    @Override
    public String getDisplayInfo() {
        double originalPrice = super.getPrice();
        double discountedPrice = getPrice();

        return super.getDisplayInfo().replace(
                String.format("Ціна: %.2f грн", originalPrice),
                String.format("Ціна: %.2f грн (було: %.2f грн)", discountedPrice, originalPrice)
        ) + String.format("\n🎯 ЗНИЖКА %.0f%%! %s", discountPercentage, reason);
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public String getReason() {
        return reason;
    }
}
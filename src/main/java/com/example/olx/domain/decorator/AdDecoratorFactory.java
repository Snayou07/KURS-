package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

public class AdDecoratorFactory {

    public static AdComponent createBasicAd(Ad ad) {
        return new BasicAdComponent(ad);
    }

    public static AdComponent createPremiumAd(Ad ad) {
        return new PremiumAdDecorator(new BasicAdComponent(ad));
    }

    public static AdComponent createUrgentAd(Ad ad) {
        return new UrgentAdDecorator(new BasicAdComponent(ad));
    }

    public static AdComponent createDiscountAd(Ad ad, double discountPercentage, String reason) {
        return new DiscountAdDecorator(new BasicAdComponent(ad), discountPercentage, reason);
    }

    public static AdComponent createWarrantyAd(Ad ad, int warrantyMonths, String warrantyType) {
        return new WarrantyAdDecorator(new BasicAdComponent(ad), warrantyMonths, warrantyType);
    }

    public static AdComponent createDeliveryAd(Ad ad, boolean freeDelivery, double deliveryCost, String deliveryInfo) {
        return new DeliveryAdDecorator(new BasicAdComponent(ad), freeDelivery, deliveryCost, deliveryInfo);
    }

    // Метод для створення повністю декорованого оголошення
    public static AdComponent createFullyDecoratedAd(Ad ad, boolean isPremium, boolean isUrgent,
                                                     Double discountPercentage, String discountReason,
                                                     Integer warrantyMonths, String warrantyType,
                                                     Boolean freeDelivery, Double deliveryCost, String deliveryInfo) {
        AdComponent component = new BasicAdComponent(ad);

        // Додаємо декоратори по черзі
        if (discountPercentage != null && discountPercentage > 0) {
            component = new DiscountAdDecorator(component, discountPercentage, discountReason);
        }

        if (warrantyMonths != null && warrantyMonths > 0) {
            component = new WarrantyAdDecorator(component, warrantyMonths, warrantyType);
        }

        if (freeDelivery != null || deliveryCost != null) {
            boolean free = freeDelivery != null && freeDelivery;
            double cost = deliveryCost != null ? deliveryCost : 0;
            component = new DeliveryAdDecorator(component, free, cost, deliveryInfo);
        }

        if (isUrgent) {
            component = new UrgentAdDecorator(component);
        }

        if (isPremium) {
            component = new PremiumAdDecorator(component);
        }

        return component;
    }

    // Виправлені методи
    public static AdComponent createDiscountAd(AdComponent baseComponent, double discountPercentage, String reason) {
        return new DiscountAdDecorator(baseComponent, discountPercentage, reason);
    }

    public static AdComponent createBaseAdComponent(Ad ad) {
        return new BasicAdComponent(ad);
    }
}
// src/main/java/com/example/olx/domain/decorator/AdDecoratorSyncUtil.java
package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

/**
 * Утиліта для синхронізації між декораторами та полями моделі Ad
 * Забезпечує узгодженість між decorator pattern та прямими полями
 */
public class AdDecoratorSyncUtil {

    /**
     * Синхронізує стан декорованого компонента з моделлю Ad
     * Оновлює поля premium, urgent в моделі на основі застосованих декораторів
     */
    public static void syncDecoratorWithModel(AdComponent decoratedComponent) {
        if (decoratedComponent == null) {
            return;
        }

        Ad ad = decoratedComponent.getAd();
        if (ad == null) {
            return;
        }

        // Перевіряємо, чи є компонент декорованим
        boolean isPremium = isPremiumDecorated(decoratedComponent);
        boolean isUrgent = isUrgentDecorated(decoratedComponent);

        // Синхронізуємо з моделлю
        ad.setPremium(isPremium);
        ad.setUrgent(isUrgent);
    }

    /**
     * Створює декорований компонент на основі полів моделі Ad
     */
    public static AdComponent createDecoratedFromModel(Ad ad) {
        if (ad == null) {
            throw new IllegalArgumentException("Ad cannot be null");
        }

        AdComponent component = new BasicAdComponent(ad);

        // Застосовуємо декоратори на основі полів моделі
        if (ad.hasDiscount() && ad.getDiscountPercentage() > 0) {
            component = new DiscountAdDecorator(component, ad.getDiscountPercentage(), ad.getDiscountReason());
        }

        if (ad.hasWarranty() && ad.getWarrantyMonths() > 0) {
            component = new WarrantyAdDecorator(component, ad.getWarrantyMonths(), ad.getWarrantyType());
        }

        if (ad.hasDelivery()) {
            component = new DeliveryAdDecorator(component, ad.isFreeDelivery(), ad.getDeliveryCost(), ad.getDeliveryInfo());
        }

        if (ad.isUrgent()) {
            component = new UrgentAdDecorator(component);
        }

        if (ad.isPremium()) {
            component = new PremiumAdDecorator(component);
        }

        return component;
    }

    /**
     * Перевіряє, чи містить компонент PremiumAdDecorator
     */
    public static boolean isPremiumDecorated(AdComponent component) {
        if (component instanceof PremiumAdDecorator) {
            return true;
        }
        if (component instanceof AdDecorator) {
            return isPremiumDecorated(((AdDecorator) component).component);
        }
        return false;
    }

    /**
     * Перевіряє, чи містить компонент UrgentAdDecorator
     */
    public static boolean isUrgentDecorated(AdComponent component) {
        if (component instanceof UrgentAdDecorator) {
            return true;
        }
        if (component instanceof AdDecorator) {
            return isUrgentDecorated(((AdDecorator) component).component);
        }
        return false;
    }

    /**
     * Перевіряє, чи містить компонент DiscountAdDecorator
     */
    public static boolean hasDiscountDecorator(AdComponent component) {
        if (component instanceof DiscountAdDecorator) {
            return true;
        }
        if (component instanceof AdDecorator) {
            return hasDiscountDecorator(((AdDecorator) component).component);
        }
        return false;
    }

    /**
     * Перевіряє, чи містить компонент WarrantyAdDecorator
     */
    public static boolean hasWarrantyDecorator(AdComponent component) {
        if (component instanceof WarrantyAdDecorator) {
            return true;
        }
        if (component instanceof AdDecorator) {
            return hasWarrantyDecorator(((AdDecorator) component).component);
        }
        return false;
    }

    /**
     * Перевіряє, чи містить компонент DeliveryAdDecorator
     */
    public static boolean hasDeliveryDecorator(AdComponent component) {
        if (component instanceof DeliveryAdDecorator) {
            return true;
        }
        if (component instanceof AdDecorator) {
            return hasDeliveryDecorator(((AdDecorator) component).component);
        }
        return false;
    }

    /**
     * Отримує інформацію про знижку з декоратора
     */
    public static Double getDiscountPercentage(AdComponent component) {
        if (component instanceof DiscountAdDecorator) {
            return ((DiscountAdDecorator) component).getDiscountPercentage();
        }
        if (component instanceof AdDecorator) {
            return getDiscountPercentage(((AdDecorator) component).component);
        }
        return null;
    }

    /**
     * Отримує причину знижки з декоратора
     */
    public static String getDiscountReason(AdComponent component) {
        if (component instanceof DiscountAdDecorator) {
            return ((DiscountAdDecorator) component).getReason();
        }
        if (component instanceof AdDecorator) {
            return getDiscountReason(((AdDecorator) component).component);
        }
        return null;
    }
}
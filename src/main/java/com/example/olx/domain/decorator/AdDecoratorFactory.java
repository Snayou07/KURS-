package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

public class AdDecoratorFactory {

    /**
     * Создает базовый компонент объявления.
     * @param ad Объявление
     * @return Базовый компонент AdComponent
     */
    public static AdComponent createBasicAd(Ad ad) {
        if (ad == null) {
            // Consider logging an error or throwing IllegalArgumentException
            return null;
        }
        return new BasicAdComponent(ad);
    }

    /**
     * Создает премиум-декоратор для объявления.
     * Включает в себя базовый компонент.
     * @param ad Объявление
     * @return AdComponent, декорированный как Premium
     */
    public static AdComponent createPremiumAd(Ad ad) {
        if (ad == null) return null;
        return new PremiumAdDecorator(new BasicAdComponent(ad));
    }

    /**
     * Создает срочный-декоратор для объявления.
     * Включает в себя базовый компонент.
     * @param ad Объявление
     * @return AdComponent, декорированный как Urgent
     */
    public static AdComponent createUrgentAd(Ad ad) {
        if (ad == null) return null;
        // Предполагается, что UrgentAdDecorator(AdComponent) существует
        return new UrgentAdDecorator(new BasicAdComponent(ad));
    }

    /**
     * Создает декоратор скидки для объявления.
     * Включает в себя базовый компонент.
     * @param ad Объявление
     * @param discountPercentage Процент скидки
     * @param reason Причина скидки
     * @return AdComponent, декорированный скидкой
     */
    public static AdComponent createDiscountAd(Ad ad, double discountPercentage, String reason) {
        if (ad == null) return null;
        // Предполагается, что DiscountAdDecorator(AdComponent, double, String) существует
        return new DiscountAdDecorator(new BasicAdComponent(ad), discountPercentage, reason);
    }

    /**
     * Создает декоратор гарантии для объявления.
     * Включает в себя базовый компонент.
     * @param ad Объявление
     * @param warrantyMonths Количество месяцев гарантии
     * @param warrantyType Тип гарантии
     * @return AdComponent, декорированный гарантией
     */
    public static AdComponent createWarrantyAd(Ad ad, int warrantyMonths, String warrantyType) {
        if (ad == null) return null;
        // Предполагается, что WarrantyAdDecorator(AdComponent, int, String) существует
        return new WarrantyAdDecorator(new BasicAdComponent(ad), warrantyMonths, warrantyType);
    }

    /**
     * Создает декоратор доставки для объявления.
     * Включает в себя базовый компонент.
     * @param ad Объявление
     * @param freeDelivery Бесплатная ли доставка
     * @param deliveryCost Стоимость доставки (если не бесплатная)
     * @param deliveryInfo Информация о доставке
     * @return AdComponent, декорированный информацией о доставке
     */
    public static AdComponent createDeliveryAd(Ad ad, boolean freeDelivery, double deliveryCost, String deliveryInfo) {
        if (ad == null) return null;
        // Предполагается, что DeliveryAdDecorator(AdComponent, boolean, double, String) существует
        return new DeliveryAdDecorator(new BasicAdComponent(ad), freeDelivery, deliveryCost, deliveryInfo);
    }

    /**
     * Метод для создания полностью декорированного объявления на основе свойств Ad.
     * Предполагается, что объект Ad содержит методы для получения информации о скидках, гарантии, доставке и т.д.
     * (например, ad.getDiscountPercentage(), ad.getWarrantyMonths(), ad.isFreeDelivery())
     *
     * @param ad Базовый объект объявления.
     * @return Декорированный AdComponent.
     */
    public static AdComponent createFullyDecoratedAdFromAdProperties(Ad ad) {
        if (ad == null) {
            System.err.println("AdDecoratorFactory: Попытка декорировать null Ad объект.");
            return null;
        }

        AdComponent component = new BasicAdComponent(ad);

        // Предположим, что у Ad есть такие методы:
        // ad.hasDiscount(), ad.getDiscountPercentage(), ad.getDiscountReason()
        // ad.hasWarranty(), ad.getWarrantyMonths(), ad.getWarrantyType()
        // ad.hasDelivery(), ad.isFreeDelivery(), ad.getDeliveryCost(), ad.getDeliveryInfo()
        // ad.isUrgent()
        // ad.isPremium()

        if (ad.hasDiscount()) { // Пример: предположение о наличии метода
            component = new DiscountAdDecorator(component);
        }

        if (ad.hasWarranty()) { // Пример
            component = new WarrantyAdDecorator(component);
        }

        if (ad.hasDelivery()) { // Пример
            component = new DeliveryAdDecorator(component);
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
     * Метод для создания полностью декорированного объявления с явным указанием параметров декорации.
     * (Этот метод уже был в вашем коде и выглядит рабочим для цепочки декораторов)
     */
    public static AdComponent createFullyDecoratedAd(Ad ad, boolean isPremium, boolean isUrgent,
                                                     Double discountPercentage, String discountReason,
                                                     Integer warrantyMonths, String warrantyType,
                                                     Boolean freeDelivery, Double deliveryCost, String deliveryInfo) {
        if (ad == null) {
            System.err.println("AdDecoratorFactory: Попытка декорировать null Ad объект в createFullyDecoratedAd.");
            return null;
        }
        AdComponent component = new BasicAdComponent(ad);

        if (discountPercentage != null && discountPercentage > 0) {
            component = new DiscountAdDecorator(component, discountPercentage, (discountReason == null ? "" : discountReason));
        }

        if (warrantyMonths != null && warrantyMonths > 0) {
            component = new WarrantyAdDecorator(component, warrantyMonths, (warrantyType == null ? "" : warrantyType));
        }

        if (freeDelivery != null || deliveryCost != null) {
            boolean free = freeDelivery != null && freeDelivery;
            double cost = deliveryCost != null ? deliveryCost : 0.0;
            component = new DeliveryAdDecorator(component, free, cost, (deliveryInfo == null ? "" : deliveryInfo));
        }

        if (isUrgent) {
            component = new UrgentAdDecorator(component);
        }

        if (isPremium) {
            component = new PremiumAdDecorator(component);
        }

        return component;
    }
}
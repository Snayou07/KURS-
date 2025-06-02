// com/example/olx/domain/decorator/AdDecoratorFactory.java
package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad; // Переконайтеся, що Ad імпортовано

public class AdDecoratorFactory {

    /**
     * Створює базовий компонент оголошення.
     * @param ad Оголошення
     * @return Базовий компонент AdComponent
     */
    public static AdComponent createBasicAd(Ad ad) {
        if (ad == null) {
            System.err.println("AdDecoratorFactory: Ad не може бути null для createBasicAd.");
            // Consider logging an error or throwing IllegalArgumentException
            return null; // Або кинути виняток
        }
        return new BasicAdComponent(ad);
    }

    /**
     * Створює преміум-декоратор для оголошення.
     * Включає в себе базовий компонент.
     * @param ad Оголошення
     * @return AdComponent, декорований як Premium
     */
    public static AdComponent createPremiumAd(Ad ad) {
        if (ad == null) {
            System.err.println("AdDecoratorFactory: Ad не може бути null для createPremiumAd.");
            return null;
        }
        return new PremiumAdDecorator(new BasicAdComponent(ad));
    }

    /**
     * Створює терміновий-декоратор для оголошення.
     * Включає в себе базовий компонент.
     * @param ad Оголошення
     * @return AdComponent, декорований як Urgent
     */
    public static AdComponent createUrgentAd(Ad ad) {
        if (ad == null) {
            System.err.println("AdDecoratorFactory: Ad не може бути null для createUrgentAd.");
            return null;
        }
        return new UrgentAdDecorator(new BasicAdComponent(ad));
    }

    /**
     * Створює декоратор знижки для оголошення.
     * Включає в себе базовий компонент.
     *
     * @param ad Оголошення
     * @return AdComponent, декорований знижкою
     */
    public static AdComponent createDiscountAd(Ad ad) {
        if (ad == null) {
            System.err.println("AdDecoratorFactory: Ad не може бути null для createDiscountAd.");
            return null;
        }
        // Тепер передаємо параметри до виправленого конструктора
        return new DiscountAdDecorator(new BasicAdComponent(ad));
    }

    /**
     * Створює декоратор гарантії для оголошення.
     * Включає в себе базовий компонент.
     * @param ad Оголошення
     * @param warrantyMonths Кількість місяців гарантії
     * @param warrantyType Тип гарантії
     * @return AdComponent, декорований гарантією
     */
    public static AdComponent createWarrantyAd(Ad ad, int warrantyMonths, String warrantyType) {
        if (ad == null) {
            System.err.println("AdDecoratorFactory: Ad не може бути null для createWarrantyAd.");
            return null;
        }
        // Тепер передаємо параметри
        return new WarrantyAdDecorator(new BasicAdComponent(ad), warrantyMonths, warrantyType);
    }

    /**
     * Створює декоратор доставки для оголошення.
     * Включає в себе базовий компонент.
     * @param ad Оголошення
     * @param freeDelivery Безкоштовна чи доставка
     * @param deliveryCost Вартість доставки (якщо не безкоштовна)
     * @param deliveryInfo Інформація про доставку
     * @return AdComponent, декорований інформацією про доставку
     */
    public static AdComponent createDeliveryAd(Ad ad, boolean freeDelivery, double deliveryCost, String deliveryInfo) {
        if (ad == null) {
            System.err.println("AdDecoratorFactory: Ad не може бути null для createDeliveryAd.");
            return null;
        }
        // Тепер передаємо параметри
        return new DeliveryAdDecorator(new BasicAdComponent(ad), freeDelivery, deliveryCost, deliveryInfo);
    }

    /**
     * Метод для створення повністю декорованого оголошення на основі властивостей Ad.
     * ПРИПУСКАЄМО, що об'єкт Ad МАЄ відповідні методи get для отримання даних.
     * (наприклад, ad.getDiscountPercentage(), ad.getWarrantyMonths(), ad.isFreeDelivery())
     *
     * @param ad Базовий об'єкт оголошення.
     * @return Декорований AdComponent.
     */
    public static AdComponent createFullyDecoratedAdFromAdProperties(Ad ad) {
        if (ad == null) {
            System.err.println("AdDecoratorFactory: Спроба декорувати null Ad об'єкт в createFullyDecoratedAdFromAdProperties.");
            return null;
        }

        AdComponent component = new BasicAdComponent(ad);

        // Припускаємо, що у Ad є такі методи:
        // ad.hasDiscount() -> boolean
        // ad.getDiscountPercentage() -> double
        // ad.getDiscountReason() -> String

        // ad.hasWarranty() -> boolean
        // ad.getWarrantyMonths() -> int
        // ad.getWarrantyType() -> String

        // ad.hasDeliveryInfo() -> boolean (чи є взагалі інфо про доставку)
        // ad.isFreeDelivery() -> boolean
        // ad.getDeliveryCost() -> double
        // ad.getDeliveryDetails() -> String (для deliveryInfo)

        // ad.isUrgent() -> boolean
        // ad.isPremium() -> boolean

        // Важливо: Порядок декораторів може мати значення для getCalculatedPrice() та getDisplayInfo()

        if (ad.hasDiscount()) { // Приклад: припущення про наявність методу
            component = new DiscountAdDecorator(component, ad.getDiscountPercentage(), ad.getDiscountReason());
        }

        if (ad.hasWarranty()) { // Приклад
            component = new WarrantyAdDecorator(component, ad.getWarrantyMonths(), ad.getWarrantyType());
        }

        if (ad.hasDelivery()) { // Приклад
            component = new DeliveryAdDecorator(component, ad.isFreeDelivery(), ad.getDeliveryCost(), ad.getDeliveryDetails());
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
     * Метод для створення повністю декорованого оголошення з явним зазначенням параметрів декорації.
     */
    public static AdComponent createFullyDecoratedAd(Ad ad,
                                                     boolean isPremium,
                                                     boolean isUrgent,
                                                     Double discountPercentage, String discountReason,
                                                     Integer warrantyMonths, String warrantyType,
                                                     // Уважно перевірте імена цих параметрів у вашому коді:
                                                     Boolean hasDeliveryOpt, // Параметр, що вказує, чи є опція доставки
                                                     Boolean isItFreeDelivery, // Параметр, що вказує, чи доставка безкоштовна
                                                     Double actualDeliveryCost, // Параметр для вартості доставки
                                                     String deliveryInformation) { // Параметр для інформації про доставку
        if (ad == null) {
            System.err.println("AdDecoratorFactory: Спроба декорувати null Ad об'єкт в createFullyDecoratedAd.");
            return null;
        }
        AdComponent component = new BasicAdComponent(ad);

        // Застосовуємо декоратори в бажаному порядку

        if (discountPercentage != null && discountPercentage > 0) {
            component = new DiscountAdDecorator(component, discountPercentage, discountReason);
        }

        if (warrantyMonths != null && warrantyMonths > 0) {
            component = new WarrantyAdDecorator(component, warrantyMonths, warrantyType);
        }

        // Блок, де виникає помилка (рядок приблизно 185-191)
        // Переконайтеся, що імена, які використовуються тут, відповідають іменам параметрів методу
        if (hasDeliveryOpt != null && hasDeliveryOpt) {
            // Рядок 187 у вашому коді, ймовірно, тут:
            // Використовуйте імена параметрів, переданих у метод:
            boolean isActuallyFree = isItFreeDelivery != null && isItFreeDelivery;
            double cost = (actualDeliveryCost != null && !isActuallyFree) ? actualDeliveryCost : 0.0;
            String info = deliveryInformation != null ? deliveryInformation : "Стандартна доставка";
            component = new DeliveryAdDecorator(component, isActuallyFree, cost, info);
        }

        if (isUrgent) {
            component = new UrgentAdDecorator(component);
        }

        if (isPremium) {
            component = new PremiumAdDecorator(component);
        }

        return component;
    }

    public static AdComponent createFullyDecoratedAd(Ad ad, boolean isPremium, boolean isUrgent, Double discountPercentage, String discountReason, Integer warrantyMonths, String warrantyType, Boolean freeDelivery, Double deliveryCost, String deliveryInfo) {
        return null;
    }
}

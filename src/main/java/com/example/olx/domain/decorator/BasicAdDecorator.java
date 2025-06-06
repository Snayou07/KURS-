// src/main/java/com/example/olx/domain/decorator/BasicAdDecorator.java
package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

/**
 * Базовий декоратор для оголошень
 * Може використовуватися як проміжний декоратор або як базовий клас для інших декораторів
 */
public class BasicAdDecorator extends AdDecorator {

    public BasicAdDecorator(AdComponent component) {
        super(component);
    }

    /**
     * Альтернативний конструктор для створення з Ad
     */
    public BasicAdDecorator(Ad ad) {
        super(new BasicAdComponent(ad));
    }

    /**
     * Метод для отримання обернутого компонента
     * Повертає внутрішній компонент цього декоратора
     */
    public AdComponent getWrappedAd() {
        return this.component;
    }

    // Базова реалізація - просто делегує виклики до внутрішнього компонента
    // Всі методи вже реалізовані в AdDecorator, тому можемо не перевизначати

    @Override
    public String getTitle() {
        return super.getTitle();
    }

    @Override
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public double getPrice() {
        return super.getPrice();
    }

    @Override
    public String getDisplayInfo() {
        return super.getDisplayInfo();
    }

    @Override
    public String getFormattedTitle() {
        return super.getFormattedTitle();
    }

    @Override
    public double getCalculatedPrice() {
        return super.getCalculatedPrice();
    }

    @Override
    public Ad getAd() {
        return super.getAd();
    }

    /**
     * Статичний метод для створення BasicAdDecorator з Ad
     */
    public static BasicAdDecorator wrap(Ad ad) {
        return new BasicAdDecorator(ad);
    }

    /**
     * Статичний метод для створення BasicAdDecorator з AdComponent
     */
    public static BasicAdDecorator wrap(AdComponent component) {
        return new BasicAdDecorator(component);
    }

    /**
     * Перевірка чи це базовий декоратор
     */
    public boolean isBasicDecorator() {
        return true;
    }

    /**
     * Отримання типу декоратора
     */
    public String getDecoratorType() {
        return "Basic";
    }
}
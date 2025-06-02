package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

public class BasicAdComponent implements AdComponent {
    private final Ad ad;

    public BasicAdComponent(Ad ad) {
        if (ad == null) {
            throw new IllegalArgumentException("Ad cannot be null");
        }
        this.ad = ad;
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Назва: %s\nОпис: %s\nЦіна: %.2f грн\nСтатус: %s",
                ad.getTitle(),
                ad.getDescription() != null ? ad.getDescription() : "Без опису",
                ad.getPrice(),
                getAdStatus());
    }

    @Override
    public double getCalculatedPrice() {
        return ad.getPrice();
    }

    @Override
    public String getFormattedTitle() {
        return ad.getTitle();
    }

    @Override
    public Ad getAd() {
        return ad;
    }

    // Допоміжний метод для отримання статусу оголошення
    private String getAdStatus() {
        try {
            // Якщо у Ad є метод getStatus()
            return ad.getStatus().toString();
        } catch (Exception e) {
            // Якщо метод getStatus() не існує або повертає null
            try {
                // Спробуємо отримати стан через getState()
                if (ad.getState() != null) {
                    return ad.getState().toString();
                }
            } catch (Exception ex) {
                // Якщо і getState() не працює
            }
            return "Активне"; // Значення за замовчуванням
        }
    }
}
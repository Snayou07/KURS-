package com.example.olx.domain.model;

import java.io.Serializable; // Імпортуємо інтерфейс Serializable

public class DraftAdState implements AdState, Serializable { // Додаємо Serializable

    // Рекомендується додати serialVersionUID для контролю версій при серіалізації
    private static final long serialVersionUID = 1L;

    @Override
    public void publish(Ad ad) {
        // Перехід з чернетки до активного стану
        ad.setCurrentState(new ActiveAdState());
    }

    @Override
    public void archive(Ad ad) {
        // Можна архівувати чернетку
        ad.setCurrentState(new ArchivedAdState());
    }

    @Override
    public void markAsSold(Ad ad) {
        // Неможливо позначити чернетку як продану
        throw new IllegalStateException("Неможливо позначити чернетку як продану. Спочатку опублікуйте оголошення.");
    }

    @Override
    public String getStatusName() {
        return "Чернетка";
    }
}
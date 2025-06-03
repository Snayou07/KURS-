// src/main/java/com/example/olx/domain/model/DraftAdState.java
package com.example.olx.domain.model;

public class DraftAdState implements AdState {
    @Override
    public void publish(Ad ad) {
        ad.setCurrentState(new ActiveAdState());
    }

    @Override
    public void archive(Ad ad) {
        ad.setCurrentState(new ArchivedAdState());
    }

    @Override
    public void markAsSold(Ad ad) {
        // Не можна продати чернетку
        throw new IllegalStateException("Неможливо продати чернетку оголошення");
    }

    @Override
    public String getStatusName() {
        return "Чернетка";
    }
}
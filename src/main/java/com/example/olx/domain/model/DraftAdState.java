// src/main/java/com/example/olx/domain/model/DraftAdState.java
package com.example.olx.domain.model;

import java.io.Serializable;

public class DraftAdState implements AdState, Serializable {
    private static final long serialVersionUID = 1L;

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
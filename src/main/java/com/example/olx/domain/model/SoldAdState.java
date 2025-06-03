package com.example.olx.domain.model;

public class SoldAdState implements AdState {
    @Override
    public void publish(Ad ad) {
        throw new IllegalStateException("Неможливо опублікувати продане оголошення");
    }

    @Override
    public void archive(Ad ad) {
        ad.setCurrentState(new ArchivedAdState());
    }

    @Override
    public void markAsSold(Ad ad) {
        // Вже продано
    }

    @Override
    public String getStatusName() {
        return "Продано";
    }
}
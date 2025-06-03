package com.example.olx.domain.model;

public class ActiveAdState implements AdState {
    @Override
    public void publish(Ad ad) {
        // Вже опубліковано
    }

    @Override
    public void archive(Ad ad) {
        ad.setCurrentState(new ArchivedAdState());
    }

    @Override
    public void markAsSold(Ad ad) {
        ad.setCurrentState(new SoldAdState());
    }

    @Override
    public String getStatusName() {
        return "Активне";
    }
}
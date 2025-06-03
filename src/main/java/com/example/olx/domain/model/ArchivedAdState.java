package com.example.olx.domain.model;

public class ArchivedAdState implements AdState {
    @Override
    public void publish(Ad ad) {
        ad.setCurrentState(new ActiveAdState());
    }

    @Override
    public void archive(Ad ad) {
        // Вже архівовано
    }

    @Override
    public void markAsSold(Ad ad) {
        ad.setCurrentState(new SoldAdState());
    }

    @Override
    public String getStatusName() {
        return "Архівоване";
    }
}
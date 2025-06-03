package com.example.olx.domain.model;

public class ModerationAdState implements AdState {
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
        throw new IllegalStateException("Неможливо продати оголошення на модерації");
    }

    @Override
    public String getStatusName() {
        return "На модерації";
    }
}
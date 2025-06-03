package com.example.olx.domain.model;

import java.io.Serializable;

public class ModerationAdState implements AdState, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public void publish(Ad ad) {
        System.out.println("Оголошення '" + ad.getTitle() + "' проходить модерацію і не може бути опубліковане.");
    }

    @Override
    public void archive(Ad ad) {
        System.out.println("Оголошення на модерації '" + ad.getTitle() + "' архівується...");
        ad.setCurrentState(new ArchivedAdState());
    }

    @Override
    public void markAsSold(Ad ad) {
        System.out.println("Оголошення на модерації '" + ad.getTitle() + "' не може бути продане.");
    }

    @Override
    public String getStatusName() {
        return "На модерації";
    }
}
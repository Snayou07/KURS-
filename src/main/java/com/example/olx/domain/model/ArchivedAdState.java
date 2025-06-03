package com.example.olx.domain.model;

import java.io.Serializable;

public class ArchivedAdState implements AdState, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public void publish(Ad ad) {
        System.out.println("Архівоване оголошення '" + ad.getTitle() + "' публікується знову...");
        ad.setCurrentState(new ActiveAdState());
    }

    @Override
    public void archive(Ad ad) {
        System.out.println("Оголошення '" + ad.getTitle() + "' вже архівоване.");
    }

    @Override
    public void markAsSold(Ad ad) {
        System.out.println("Архівоване оголошення '" + ad.getTitle() + "' не може бути продане.");
    }

    @Override
    public String getStatusName() {
        return "Архівоване";
    }
}
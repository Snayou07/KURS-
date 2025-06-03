package com.example.olx.domain.model;

import java.io.Serializable;

public class ActiveAdState implements AdState, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public void publish(Ad ad) {
        System.out.println("Оголошення '" + ad.getTitle() + "' вже опубліковане.");
    }

    @Override
    public void archive(Ad ad) {
        System.out.println("Оголошення '" + ad.getTitle() + "' архівується...");
        ad.setCurrentState(new ArchivedAdState());
    }

    @Override
    public void markAsSold(Ad ad) {
        System.out.println("Оголошення '" + ad.getTitle() + "' позначено як продане.");
        ad.setCurrentState(new SoldAdState());
    }

    @Override
    public String getStatusName() {
        return "Активне";
    }
}
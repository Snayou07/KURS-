// src/main/java/com/example/olx/domain/model/ActiveAdState.java
package com.example.olx.domain.model;

public class ActiveAdState implements AdState {
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
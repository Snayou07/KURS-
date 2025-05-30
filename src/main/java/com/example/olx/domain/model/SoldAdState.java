// src/main/java/com/example/olx/domain/model/SoldAdState.java
package com.example.olx.domain.model;

public class SoldAdState implements AdState {
    @Override
    public void publish(Ad ad) {
        System.out.println("Продане оголошення '" + ad.getTitle() + "' не може бути опубліковане знову.");
    }

    @Override
    public void archive(Ad ad) {
        System.out.println("Продане оголошення '" + ad.getTitle() + "' архівується...");
        ad.setCurrentState(new ArchivedAdState());
    }

    @Override
    public void markAsSold(Ad ad) {
        System.out.println("Оголошення '" + ad.getTitle() + "' вже позначене як продане.");
    }

    @Override
    public String getStatusName() {
        return "Продано";
    }
}
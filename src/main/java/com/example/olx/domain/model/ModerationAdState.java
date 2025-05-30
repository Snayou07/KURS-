// src/main/java/com/example/olx/domain/model/ModerationAdState.java
package com.example.olx.domain.model;

public class ModerationAdState implements AdState {
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
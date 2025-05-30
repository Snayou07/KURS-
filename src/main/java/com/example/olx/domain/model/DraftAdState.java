// src/main/java/com/example/olx/domain/model/DraftAdState.java
package com.example.olx.domain.model;

public class DraftAdState implements AdState {
    @Override
    public void publish(Ad ad) {
        System.out.println("Оголошення '" + ad.getTitle() + "' публікується...");
        ad.setCurrentState(new ActiveAdState());
    }

    @Override
    public void archive(Ad ad) {
        System.out.println("Оголошення '" + ad.getTitle() + "' не може бути архівоване з чернетки.");
    }

    @Override
    public void markAsSold(Ad ad) {
        System.out.println("Оголошення '" + ad.getTitle() + "' не може бути продане з чернетки.");
    }

    @Override
    public String getStatusName() {
        return "Чернетка";
    }
}

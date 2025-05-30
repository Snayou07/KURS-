// src/main/java/com/example/olx/application/command/PublishAdCommand.java
package com.example.olx.application.command;

import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;

public class PublishAdCommand implements Command {
    private final AdServicePort adService;
    private final String adId;
    private Ad ad;
    private String previousState;

    public PublishAdCommand(AdServicePort adService, String adId) {
        this.adService = adService;
        this.adId = adId;
    }

    @Override
    public void execute() throws UserNotFoundException {
        this.ad = adService.getAdById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Оголошення з ID " + adId + " не знайдено"));

        this.previousState = ad.getStatus();
        ad.publishAd();
        System.out.println("Команда PublishAd виконана для оголошення: " + ad.getTitle());
    }

    @Override
    public void undo() throws UserNotFoundException {
        if (ad != null && previousState != null) {
            System.out.println("Скасування публікації оголошення: " + ad.getTitle());
            restorePreviousState();
        }
    }

    private void restorePreviousState() {
        switch (previousState) {
            case "Чернетка":
                // Потрібно мати метод для повернення в стан чернетки
                ad.setCurrentState(new com.example.olx.domain.model.DraftAdState());
                break;
            case "Архівоване":
                ad.archiveAd();
                break;
            case "На модерації":
                ad.setCurrentState(new com.example.olx.domain.model.ModerationAdState());
                break;
            default:
                System.err.println("Не вдалося відновити попередній стан: " + previousState);
        }
    }

    @Override
    public String getDescription() {
        return "Публікація оголошення ID: " + adId;
    }
}
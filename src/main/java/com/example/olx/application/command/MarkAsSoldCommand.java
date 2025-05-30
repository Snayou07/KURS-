// MarkAsSoldCommand.java
package com.example.olx.application.command;

import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;

public class MarkAsSoldCommand implements Command {
    private final AdServicePort adService;
    private final String adId;
    private Ad ad;
    private String previousState;

    public MarkAsSoldCommand(AdServicePort adService, String adId) {
        this.adService = adService;
        this.adId = adId;
    }

    @Override
    public void execute() throws UserNotFoundException {
        this.ad = adService.getAdById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Оголошення з ID " + adId + " не знайдено"));

        this.previousState = ad.getStatus();
        ad.markAsSold();
        System.out.println("Команда MarkAsSold виконана для оголошення: " + ad.getTitle());
    }

    @Override
    public void undo() throws UserNotFoundException {
        if (ad != null && previousState != null) {
            System.out.println("Скасування позначення як продане оголошення: " + ad.getTitle());
            restorePreviousState();
        }
    }

    private void restorePreviousState() {
        switch (previousState) {
            case "Чернетка":
                ad.setCurrentState(new com.example.olx.domain.model.DraftAdState());
                break;
            case "Активне":
                ad.setCurrentState(new com.example.olx.domain.model.ActiveAdState());
                break;
            case "Архівоване":
                ad.setCurrentState(new com.example.olx.domain.model.ArchivedAdState());
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
        return "Позначення як продане оголошення ID: " + adId;
    }
}


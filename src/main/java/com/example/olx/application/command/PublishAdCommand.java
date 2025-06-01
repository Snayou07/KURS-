package com.example.olx.application.command;

import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.AdState;

public class PublishAdCommand implements Command {
    private final AdServicePort adService;
    private final String adId;
    private AdState previousStatus;

    public PublishAdCommand(AdServicePort adService, String adId) {
        this.adService = adService;
        this.adId = adId;
    }

    @Override
    public void execute() throws UserNotFoundException {
        Ad ad = adService.getAdById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Оголошення з ID " + adId + " не знайдено"));

        this.previousStatus = ad.getCurrentState();
        adService.publishAd(adId);
        System.out.println("Команда PublishAd виконана для оголошення ID: " + adId);
    }

    @Override
    public void undo() throws UserNotFoundException {
        if (previousStatus != null) {
            // Повертаємо попередній статус
            adService.changeAdState(adId, previousStatus);
            System.out.println("Команда PublishAd скасована для оголошення ID: " + adId);
        }
    }

    @Override
    public String getDescription() {
        return "Публікація оголошення ID: " + adId;
    }

}
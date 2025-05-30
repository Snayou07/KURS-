package com.example.olx.application.command;

import com.example.olx.application.service.port.AdServicePort;
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
    public void execute() {
        this.ad = adService.getAdById(adId).orElse(null);
        if (ad != null) {
            this.previousState = ad.getStatus();
            ad.publishAd();
            System.out.println("Команда PublishAd виконана для оголошення: " + ad.getTitle());
        }
    }

    @Override
    public void undo() {
        if (ad != null && previousState != null) {
            // Складніше відновити попередній стан, потрібно мапити стани
            System.out.println("Скасування публікації оголошення: " + ad.getTitle());
            // Тут би потрібна логіка для встановлення попереднього стану
        }
    }

    @Override
    public String getDescription() {
        return "Публікація оголошення ID: " + adId;
    }
}
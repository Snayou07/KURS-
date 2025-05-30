// src/main/java/com/example/olx/application/command/DeleteAdCommand.java
package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;

public class DeleteAdCommand implements Command {
    private final AdServicePort adService;
    private final String adId;
    private final String currentUserId;
    private Ad deletedAd;

    public DeleteAdCommand(AdServicePort adService, String adId, String currentUserId) {
        this.adService = adService;
        this.adId = adId;
        this.currentUserId = currentUserId;
    }

    @Override
    public void execute() throws UserNotFoundException {
        // Зберігаємо дані перед видаленням
        this.deletedAd = adService.getAdById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Оголошення з ID " + adId + " не знайдено"));

        adService.deleteAd(adId, currentUserId);
        System.out.println("Команда DeleteAd виконана для оголошення: " + deletedAd.getTitle());
    }

    @Override
    public void undo() throws UserNotFoundException {
        if (deletedAd != null) {
            // Відновлюємо оголошення
            AdCreationRequest restoreRequest = new AdCreationRequest(
                    deletedAd.getTitle(),
                    deletedAd.getDescription(),
                    deletedAd.getPrice(),
                    deletedAd.getCategoryId(),
                    deletedAd.getSellerId(),
                    deletedAd.getImagePaths()
            );
            Ad restoredAd = adService.createAd(restoreRequest);
            System.out.println("Команда DeleteAd скасована для оголошення: " + deletedAd.getTitle());

            // Намагаємося відновити попередній стан (якщо можливо)
            if (!deletedAd.getStatus().equals("Чернетка")) {
                try {
                    restoreAdState(restoredAd, deletedAd.getStatus());
                } catch (Exception e) {
                    System.err.println("Не вдалося відновити стан оголошення: " + e.getMessage());
                }
            }
        }
    }

    private void restoreAdState(Ad ad, String previousStatus) {
        switch (previousStatus) {
            case "Активне":
                ad.publishAd();
                break;
            case "Архівоване":
                ad.archiveAd();
                break;
            case "Продано":
                ad.markAsSold();
                break;
            case "На модерації":
                // Складніше відновити стан модерації, можливо потрібна окрема логіка
                break;
        }
    }

    @Override
    public String getDescription() {
        return "Видалення оголошення ID: " + adId;
    }
}
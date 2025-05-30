package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.domain.model.Ad;

public class DeleteAdCommand implements Command {
    private final AdServicePort adService;
    private final String adId;
    private final String currentUserId;
    private Ad deletedAd; // Для можливості скасування

    public DeleteAdCommand(AdServicePort adService, String adId, String currentUserId) {
        this.adService = adService;
        this.adId = adId;
        this.currentUserId = currentUserId;
    }

    @Override
    public void execute() {
        // Зберігаємо дані перед видаленням
        this.deletedAd = adService.getAdById(adId).orElse(null);
        adService.deleteAd(adId, currentUserId);
        if (deletedAd != null) {
            System.out.println("Команда DeleteAd виконана для оголошення: " + deletedAd.getTitle());
        }
    }

    @Override
    public void undo() {
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
            adService.createAd(restoreRequest);
            System.out.println("Команда DeleteAd скасована для оголошення: " + deletedAd.getTitle());
        }
    }

    @Override
    public String getDescription() {
        return "Видалення оголошення ID: " + adId;
    }
}
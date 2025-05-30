package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.domain.model.Ad;

public class UpdateAdCommand implements Command {
    private final AdServicePort adService;
    private final String adId;
    private final AdCreationRequest newRequest;
    private final String currentUserId;
    private Ad originalAd; // Для скасування
    private Ad updatedAd;

    public UpdateAdCommand(AdServicePort adService, String adId, AdCreationRequest newRequest, String currentUserId) {
        this.adService = adService;
        this.adId = adId;
        this.newRequest = newRequest;
        this.currentUserId = currentUserId;
    }

    @Override
    public void execute() {
        // Зберігаємо оригінальний стан для можливості скасування
        this.originalAd = adService.getAdById(adId).orElse(null);
        this.updatedAd = adService.updateAd(adId, newRequest, currentUserId);
        System.out.println("Команда UpdateAd виконана для оголошення: " + updatedAd.getTitle());
    }

    @Override
    public void undo() {
        if (originalAd != null) {
            // Створюємо запит на основі оригінальних даних
            AdCreationRequest originalRequest = new AdCreationRequest(
                    originalAd.getTitle(),
                    originalAd.getDescription(),
                    originalAd.getPrice(),
                    originalAd.getCategoryId(),
                    originalAd.getSellerId(),
                    originalAd.getImagePaths()
            );
            adService.updateAd(adId, originalRequest, currentUserId);
            System.out.println("Команда UpdateAd скасована для оголошення: " + originalAd.getTitle());
        }
    }

    @Override
    public String getDescription() {
        return "Оновлення оголошення ID: " + adId;
    }
}

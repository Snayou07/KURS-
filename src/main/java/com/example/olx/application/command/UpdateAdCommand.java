// src/main/java/com/example/olx/application/command/UpdateAdCommand.java
package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;

public class UpdateAdCommand implements CommandWithResult<Ad> {
    private final AdServicePort adService;
    private final String adId;
    private final AdCreationRequest newRequest;
    private final String currentUserId;
    private Ad originalAd;
    private Ad updatedAd;

    public UpdateAdCommand(AdServicePort adService, String adId, AdCreationRequest newRequest, String currentUserId) {
        this.adService = adService;
        this.adId = adId;
        this.newRequest = newRequest;
        this.currentUserId = currentUserId;
    }

    @Override
    public void execute() throws UserNotFoundException {
        // Зберігаємо оригінальний стан для можливості скасування
        this.originalAd = adService.getAdById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Оголошення з ID " + adId + " не знайдено"));

        // Створюємо копію для збереження оригінального стану
        this.originalAd = createAdCopy(this.originalAd);

        this.updatedAd = adService.updateAd(adId, newRequest, currentUserId);
        System.out.println("Команда UpdateAd виконана для оголошення: " + updatedAd.getTitle());
    }

    @Override
    public void undo() throws UserNotFoundException {
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
            this.updatedAd = adService.updateAd(adId, originalRequest, currentUserId);
            System.out.println("Команда UpdateAd скасована для оголошення: " + originalAd.getTitle());
        }
    }

    private Ad createAdCopy(Ad original) {
        try {
            return original.clone();
        } catch (CloneNotSupportedException e) {
            // Створюємо новий об'єкт з тими самими даними
            Ad copy = new Ad(
                    original.getTitle(),
                    original.getDescription(),
                    original.getPrice(),
                    original.getCategoryId(),
                    original.getSellerId(),
                    original.getImagePaths()
            );
            copy.setStatus(original.getStatus());
            return copy;
        }
    }

    @Override
    public String getDescription() {
        return "Оновлення оголошення ID: " + adId;
    }

    @Override
    public Ad getResult() {
        return updatedAd;
    }
}
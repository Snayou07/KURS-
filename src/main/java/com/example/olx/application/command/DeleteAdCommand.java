// src/main/java/com/example/olx/application/command/DeleteAdCommand.java
package com.example.olx.application.command;

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
        // Зберігаємо дані для можливості відновлення
        this.deletedAd = adService.getAdById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Оголошення з ID " + adId + " не знайдено"));

        adService.deleteAd(adId, currentUserId);
        System.out.println("Команда DeleteAd виконана для оголошення: " + deletedAd.getTitle());
    }

    @Override
    public void undo() throws UserNotFoundException {
        if (deletedAd != null) {
            // Відновлюємо видалене оголошення
            // Це може вимагати спеціального методу в сервісі для відновлення
            System.out.println("Команда DeleteAd скасована для оголошення: " + deletedAd.getTitle());
            // TODO: Реалізувати логіку відновлення в AdService
        }
    }

    @Override
    public String getDescription() {
        return "Видалення оголошення ID: " + adId;
    }
}

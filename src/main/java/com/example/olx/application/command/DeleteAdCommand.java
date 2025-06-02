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
        // Зберігаємо повну копію для відновлення
        this.deletedAd = adService.getAdById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Оголошення з ID " + adId + " не знайдено"));

        // Створюємо глибоку копію
        this.deletedAd = createAdCopy(this.deletedAd);

        adService.deleteAd(adId, currentUserId);
        System.out.println("Команда DeleteAd виконана для оголошення: " + deletedAd.getTitle());
    }

    @Override
    public void undo() throws UserNotFoundException {
        if (deletedAd != null) {
            // ✅ Відновлюємо через створення нового оголошення з тими ж даними
            AdCreationRequest restoreRequest = new AdCreationRequest(
                    deletedAd.getTitle(),
                    deletedAd.getDescription(),
                    deletedAd.getPrice(),
                    deletedAd.getCategoryId(),
                    deletedAd.getSellerId(),
                    deletedAd.getImagePaths()
            );

            // Створюємо нове оголошення з оригінальними даними
            Ad restoredAd = adService.createAd(restoreRequest);
            System.out.println("Команда DeleteAd скасована - відновлено оголошення: " + deletedAd.getTitle());
        }
    }

    private Ad createAdCopy(Ad original) {
        Ad copy = new Ad(
                original.getTitle(),
                original.getDescription(),
                original.getPrice(),
                original.getCategoryId(),
                original.getSellerId(),
                original.getImagePaths()
        );
        // Не встановлюємо ID, оскільки setAdId може не існувати
        copy.setStatus(original.getStatus());
        copy.setCurrentState(original.getCurrentState());
        return copy;
    }

    @Override
    public String getDescription() {
        return "Видалення оголошення ID: " + adId;
    }
}
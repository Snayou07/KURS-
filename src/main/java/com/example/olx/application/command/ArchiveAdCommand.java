package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;

public class ArchiveAdCommand implements Command {
    private final AdServicePort adService;
    private final String adId;
    private Ad ad;
    private String previousState;

    public ArchiveAdCommand(AdServicePort adService, String adId) {
        this.adService = adService;
        this.adId = adId;
    }

    @Override
    public void execute() throws UserNotFoundException {
        this.ad = adService.getAdById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Оголошення з ID " + adId + " не знайдено"));

        this.previousState = ad.getStatus();
        ad.archiveAd();

        // ✅ КРИТИЧНО: Використовуємо існуючий метод оновлення
        // Створюємо запит для збереження поточного стану
        AdCreationRequest updateRequest = createRequestFromAd(ad);
        adService.updateAd(adId, updateRequest, ad.getSellerId());

        System.out.println("Команда ArchiveAd виконана для оголошення: " + ad.getTitle());
    }

    @Override
    public void undo() throws UserNotFoundException {
        if (ad != null && previousState != null) {
            System.out.println("Скасування архівації оголошення: " + ad.getTitle());
            restorePreviousState();

            // ✅ КРИТИЧНО: Зберігаємо відновлений стан
            AdCreationRequest updateRequest = createRequestFromAd(ad);
            adService.updateAd(adId, updateRequest, ad.getSellerId());
        }
    }

    private AdCreationRequest createRequestFromAd(Ad ad) {
        return new AdCreationRequest(
                ad.getTitle(),
                ad.getDescription(),
                ad.getPrice(),
                ad.getCategoryId(),
                ad.getSellerId(),
                ad.getImagePaths()
        );
    }

    private void restorePreviousState() {
        switch (previousState) {
            case "Чернетка":
                ad.setCurrentState(new com.example.olx.domain.model.DraftAdState());
                break;
            case "Активне":
                ad.setCurrentState(new com.example.olx.domain.model.ActiveAdState());
                break;
            case "На модерації":
                ad.setCurrentState(new com.example.olx.domain.model.ModerationAdState());
                break;
            case "Продано":
                ad.setCurrentState(new com.example.olx.domain.model.SoldAdState());
                break;
            default:
                System.err.println("Не вдалося відновити попередній стан: " + previousState);
        }
    }

    @Override
    public String getDescription() {
        return "Архівація оголошення ID: " + adId;
    }
}
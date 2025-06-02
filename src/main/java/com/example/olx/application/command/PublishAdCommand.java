package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
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
        ad.publishAd();

        // ✅ КРИТИЧНО: Зберігаємо зміни використовуючи updateAd
        AdCreationRequest updateRequest = createRequestFromAd(ad);
        adService.updateAd(adId, updateRequest, ad.getSellerId());

        System.out.println("Команда PublishAd виконана для оголошення ID: " + adId);
    }

    @Override
    public void undo() throws UserNotFoundException {
        if (previousStatus != null) {
            Ad ad = adService.getAdById(adId)
                    .orElseThrow(() -> new IllegalArgumentException("Оголошення з ID " + adId + " не знайдено"));

            ad.setCurrentState(previousStatus);

            // ✅ КРИТИЧНО: Зберігаємо відновлений стан
            AdCreationRequest updateRequest = createRequestFromAd(ad);
            adService.updateAd(adId, updateRequest, ad.getSellerId());

            System.out.println("Команда PublishAd скасована для оголошення ID: " + adId);
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

    @Override
    public String getDescription() {
        return "Публікація оголошення ID: " + adId;
    }
}
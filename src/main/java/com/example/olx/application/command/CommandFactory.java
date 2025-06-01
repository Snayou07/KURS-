package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.domain.model.Ad;

/**
 * Фабрика для створення команд
 */
public class CommandFactory {
    private final AdServicePort adService;

    public CommandFactory(AdServicePort adService) {
        this.adService = adService;
    }

    public CommandWithResult<Ad> createCreateAdCommand(AdCreationRequest request) {
        return new CreateAdCommand(adService, request);
    }

    public CommandWithResult<Ad> createUpdateAdCommand(String adId, AdCreationRequest request, String currentUserId) {
        return new UpdateAdCommand(adService, adId, request, currentUserId);
    }

    public Command createDeleteAdCommand(String adId, String currentUserId) {
        return new DeleteAdCommand(adService, adId, currentUserId);
    }

    public Command createPublishAdCommand(String adId) {
        return new PublishAdCommand(adService, adId);
    }

    public Command createArchiveAdCommand(String adId) {
        // TODO: Реалізувати ArchiveAdCommand
        return new PublishAdCommand(adService, adId); // Заглушка
    }

    public Command createMarkAsSoldCommand(String adId) {
        // TODO: Реалізувати MarkAsSoldCommand
        return new PublishAdCommand(adService, adId); // Заглушка
    }
}
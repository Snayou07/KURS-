package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.application.service.port.AdServicePort;

/**
 * Фабрика для створення команд
 */
public class CommandFactory {
    private final AdServicePort adService;

    public CommandFactory(AdServicePort adService) {
        this.adService = adService;
    }

    public Command createCreateAdCommand(AdCreationRequest request) {
        return new CreateAdCommand(adService, request);
    }

    public Command createUpdateAdCommand(String adId, AdCreationRequest request, String currentUserId) {
        return new UpdateAdCommand(adService, adId, request, currentUserId);
    }

    public Command createDeleteAdCommand(String adId, String currentUserId) {
        return new DeleteAdCommand(adService, adId, currentUserId);
    }

    public Command createPublishAdCommand(String adId) {
        return new PublishAdCommand(adService, adId);
    }

    public Command createArchiveAdCommand(String adId) {
        return new ArchiveAdCommand(adService, adId);
    }

    public Command createMarkAsSoldCommand(String adId) {
        return new MarkAsSoldCommand(adService, adId);
    }
}
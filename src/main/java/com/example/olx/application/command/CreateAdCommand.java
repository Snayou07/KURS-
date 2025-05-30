package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;

public class CreateAdCommand implements Command {
    private final AdServicePort adService;
    private final AdCreationRequest request;
    private Ad createdAd; // Для можливості скасування

    public CreateAdCommand(AdServicePort adService, AdCreationRequest request) {
        this.adService = adService;
        this.request = request;
    }

    @Override
    public void execute() throws UserNotFoundException {
        this.createdAd = adService.createAd(request);
        System.out.println("Команда CreateAd виконана для оголошення: " + createdAd.getTitle());
    }

    @Override
    public void undo() throws UserNotFoundException {
        if (createdAd != null) {
            adService.deleteAd(createdAd.getAdId(), request.getSellerId());
            System.out.println("Команда CreateAd скасована для оголошення: " + createdAd.getTitle());
        }
    }

    @Override
    public String getDescription() {
        return "Створення оголошення: " + request.getTitle();
    }
}
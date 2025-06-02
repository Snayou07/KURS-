package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;

public class CreateAdCommand implements CommandWithResult<Ad> {
    private final AdServicePort adService;
    private final AdCreationRequest request;
    private Ad createdAd;

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
            adService.deleteAd((String) createdAd.getId(), request.getSellerId());
            System.out.println("Команда CreateAd скасована для оголошення: " + createdAd.getTitle());
            createdAd = null;
        }
    }

    @Override
    public String getDescription() {
        return "Створення оголошення: " + request.getTitle();
    }

    @Override
    public Ad getResult() {
        return createdAd;
    }
}
package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.domain.exception.UserNotFoundException;

import java.util.List;

/**
 * Високорівневий менеджер для роботи з командами оголошень
 */
public class AdCommandManager {
    private final CommandInvoker commandInvoker;
    private final CommandFactory commandFactory;

    public AdCommandManager(CommandInvoker commandInvoker, CommandFactory commandFactory) {
        this.commandInvoker = commandInvoker;
        this.commandFactory = commandFactory;
    }

    // Створення оголошення
    public void createAd(AdCreationRequest request) throws UserNotFoundException {
        Command command = commandFactory.createCreateAdCommand(request);
        commandInvoker.executeCommand(command);
    }

    // Оновлення оголошення
    public void updateAd(String adId, AdCreationRequest request, String currentUserId) throws UserNotFoundException {
        Command command = commandFactory.createUpdateAdCommand(adId, request, currentUserId);
        commandInvoker.executeCommand(command);
    }

    // Видалення оголошення
    public void deleteAd(String adId, String currentUserId) throws UserNotFoundException {
        Command command = commandFactory.createDeleteAdCommand(adId, currentUserId);
        commandInvoker.executeCommand(command);
    }

    // Публікація оголошення
    public void publishAd(String adId) throws UserNotFoundException {
        Command command = commandFactory.createPublishAdCommand(adId);
        commandInvoker.executeCommand(command);
    }

    // Архівація оголошення
    public void archiveAd(String adId) throws UserNotFoundException {
        Command command = commandFactory.createArchiveAdCommand(adId);
        commandInvoker.executeCommand(command);
    }

    // Позначення як продане
    public void markAsSold(String adId) throws UserNotFoundException {
        Command command = commandFactory.createMarkAsSoldCommand(adId);
        commandInvoker.executeCommand(command);
    }

    // Створення та публікація оголошення одночасно (використовуючи MacroCommand)
    public void createAndPublishAd(AdCreationRequest request) throws UserNotFoundException {
        // Створюємо макрокоманду для складної операції
        List<Command> commands = List.of(
                commandFactory.createCreateAdCommand(request)
                // Публікацію додамо після отримання ID створеного оголошення
        );

        // Для складних операцій краще використовувати окремі виклики
        createAd(request);

        // TODO: Тут потрібно отримати ID створеного оголошення та опублікувати його
        // Це можна зробити через додаткову логіку в CreateAdCommand або через callback
    }

    // Виконання макрокоманди
    public void executeMacroCommand(List<Command> commands, String description) throws UserNotFoundException {
        MacroCommand macroCommand = new MacroCommand(commands, description);
        commandInvoker.executeCommand(macroCommand);
    }

    // Делегування до CommandInvoker
    public void undo() throws UserNotFoundException {
        commandInvoker.undo();
    }

    public void redo() throws UserNotFoundException {
        commandInvoker.redo();
    }

    public boolean canUndo() {
        return commandInvoker.canUndo();
    }

    public boolean canRedo() {
        return commandInvoker.canRedo();
    }

    public void clearHistory() {
        commandInvoker.clearHistory();
    }

    public List<String> getCommandHistory() {
        return commandInvoker.getCommandHistory();
    }

    // Додаткові методи для GUI
    public String getLastExecutedCommandDescription() {
        List<String> history = getCommandHistory();
        return history.isEmpty() ? "Немає виконаних команд" : history.get(history.size() - 1);
    }

    public int getHistorySize() {
        return getCommandHistory().size();
    }

    public boolean hasCommands() {
        return !getCommandHistory().isEmpty();
    }
}
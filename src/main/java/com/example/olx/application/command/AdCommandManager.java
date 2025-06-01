package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;

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

    /**
     * Виконує команду з результатом і повертає результат
     */
    private <T> T executeCommandWithResult(CommandWithResult<T> command) throws UserNotFoundException {
        commandInvoker.executeCommand(command);
        return command.getResult();
    }

    /**
     * Створення оголошення
     */
    public Ad createAd(AdCreationRequest request) throws UserNotFoundException {
        CommandWithResult<Ad> command = commandFactory.createCreateAdCommand(request);
        return executeCommandWithResult(command);
    }

    /**
     * Оновлення оголошення
     */
    public Ad updateAd(String adId, AdCreationRequest request, String currentUserId) throws UserNotFoundException {
        CommandWithResult<Ad> command = commandFactory.createUpdateAdCommand(adId, request, currentUserId);
        return executeCommandWithResult(command);
    }

    /**
     * Видалення оголошення
     */
    public void deleteAd(String adId, String currentUserId) throws UserNotFoundException {
        Command command = commandFactory.createDeleteAdCommand(adId, currentUserId);
        commandInvoker.executeCommand(command);
    }

    /**
     * Публікація оголошення
     */
    public void publishAd(String adId) throws UserNotFoundException {
        Command command = commandFactory.createPublishAdCommand(adId);
        commandInvoker.executeCommand(command);
    }

    /**
     * Архівація оголошення
     */
    public void archiveAd(String adId) throws UserNotFoundException {
        Command command = commandFactory.createArchiveAdCommand(adId);
        commandInvoker.executeCommand(command);
    }

    /**
     * Позначення як продане
     */
    public void markAsSold(String adId) throws UserNotFoundException {
        Command command = commandFactory.createMarkAsSoldCommand(adId);
        commandInvoker.executeCommand(command);
    }

    /**
     * Створення та публікація оголошення одночасно
     */
    public Ad createAndPublishAd(AdCreationRequest request) throws UserNotFoundException {
        // Створюємо оголошення
        Ad createdAd = createAd(request);

        // Публікуємо створене оголошення
        if (createdAd != null) {
            publishAd(createdAd.getAdId());
        }

        return createdAd;
    }

    /**
     * Створення та публікація оголошення як макрокоманда (атомарна операція)
     */
    public Ad createAndPublishAdAtomic(AdCreationRequest request) throws UserNotFoundException {
        // Створюємо команди
        CommandWithResult<Ad> createCommand = commandFactory.createCreateAdCommand(request);

        // Для макрокоманди нам потрібно знати ID оголошення наперед
        // Цей підхід вимагає модифікації архітектури для передачі ID між командами

        // Альтернативно, виконуємо як окремі операції
        return createAndPublishAd(request);
    }

    /**
     * Виконання макрокоманди
     */
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

package com.example.olx.application.command;

import com.example.olx.application.dto.AdCreationRequest;
import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.Ad;
import java.util.List;
import java.util.ArrayList;

import static com.example.olx.presentation.gui.MainGuiApp.adService;

public class AdCommandManager {
    private final CommandInvoker commandInvoker;
    private final CommandFactory commandFactory;

    public AdCommandManager(CommandInvoker commandInvoker, CommandFactory commandFactory) {
        this.commandInvoker = commandInvoker;
        this.commandFactory = commandFactory;
    }

    // ✅ Виправлена атомарна операція створення та публікації
    public Ad createAndPublishAdAtomic(AdCreationRequest request) throws UserNotFoundException {
        List<Command> commands = new ArrayList<>();

        // Создаем команды
        CommandWithResult<Ad> createCommand = commandFactory.createCreateAdCommand(request);

        // НЕ выполняем createCommand отдельно!
        // Добавляем в макрокоманду для атомарного выполнения
        commands.add(createCommand);

        // Выполняем макрокоманду
        executeMacroCommand(commands, "Створення оголошення: " + request.getTitle());

        // Получаем результат после выполнения
        Ad createdAd = createCommand.getResult();

        if (createdAd != null) {
            // Теперь добавляем команду публикации
            Command publishCommand = commandFactory.createPublishAdCommand(createdAd.getAdId());
            List<Command> publishCommands = new ArrayList<>();
            publishCommands.add(publishCommand);

            executeMacroCommand(publishCommands, "Публікація оголошення: " + createdAd.getTitle());
        }

        return createdAd;
    }
    // Добавьте этот метод в AdCommandManager для отладки
    public void debugAdStates() {
        System.out.println("=== DEBUG: История команд ===");
        List<String> history = getCommandHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i + 1) + ". " + history.get(i));
        }

        System.out.println("=== DEBUG: Текущая позиция в истории ===");
        System.out.println("Можно отменить: " + canUndo());
        System.out.println("Можно повторить: " + canRedo());
        System.out.println("Размер истории: " + getHistorySize());
    }

    // И этот метод в сервисном слое для проверки статусов объявлений
    public void debugAllAdsStatus() {
        // Получить все объявления и вывести их статусы
        List<Ad> allAds = adService.getAllAds(); // предполагается такой метод

        System.out.println("=== DEBUG: Статусы всех объявлений ===");
        for (Ad ad : allAds) {
            System.out.println("ID: " + ad.getAdId() +
                    ", Title: " + ad.getTitle() +
                    ", Status: " + ad.getStatus() +
                    ", State: " + ad.getCurrentState().getClass().getSimpleName());
        }
    }
    private <T> T executeCommandWithResult(CommandWithResult<T> command) throws UserNotFoundException {
        commandInvoker.executeCommand(command);
        return command.getResult();
    }

    public Ad createAd(AdCreationRequest request) throws UserNotFoundException {
        CommandWithResult<Ad> command = commandFactory.createCreateAdCommand(request);
        return executeCommandWithResult(command);
    }

    public Ad updateAd(String adId, AdCreationRequest request, String currentUserId) throws UserNotFoundException {
        CommandWithResult<Ad> command = commandFactory.createUpdateAdCommand(adId, request, currentUserId);
        return executeCommandWithResult(command);
    }

    public void deleteAd(String adId, String currentUserId) throws UserNotFoundException {
        Command command = commandFactory.createDeleteAdCommand(adId, currentUserId);
        commandInvoker.executeCommand(command);
    }

    public void publishAd(String adId) throws UserNotFoundException {
        Command command = commandFactory.createPublishAdCommand(adId);
        commandInvoker.executeCommand(command);
    }

    public void archiveAd(String adId) throws UserNotFoundException {
        Command command = commandFactory.createArchiveAdCommand(adId);
        commandInvoker.executeCommand(command);
    }

    public void markAsSold(String adId) throws UserNotFoundException {
        Command command = commandFactory.createMarkAsSoldCommand(adId);
        commandInvoker.executeCommand(command);
    }

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
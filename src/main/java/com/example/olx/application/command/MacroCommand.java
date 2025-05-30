package com.example.olx.application.command;

import com.example.olx.domain.exception.UserNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Макрокоманда для виконання кількох команд разом
 */
public class MacroCommand implements Command {
    private final List<Command> commands;
    private final String description;

    public MacroCommand(List<Command> commands, String description) {
        this.commands = new ArrayList<>(commands);
        this.description = description;
    }

    @Override
    public void execute() throws UserNotFoundException {
        for (Command command : commands) {
            command.execute();
        }
    }

    @Override
    public void undo() throws UserNotFoundException {
        // Скасовуємо команди у зворотному порядку
        List<Command> reversedCommands = new ArrayList<>(commands);
        Collections.reverse(reversedCommands);

        for (Command command : reversedCommands) {
            command.undo();
        }
    }

    @Override
    public String getDescription() {
        return description + " (містить " + commands.size() + " команд)";
    }

    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
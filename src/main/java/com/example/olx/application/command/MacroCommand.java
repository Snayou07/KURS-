package com.example.olx.application.command;

import com.example.olx.domain.exception.UserNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Макрокоманда для виконання групи команд як єдиної операції
 */
public class MacroCommand implements Command {
    private final List<Command> commands;
    private final String description;
    private final List<Command> executedCommands = new ArrayList<>();

    public MacroCommand(List<Command> commands, String description) {
        this.commands = new ArrayList<>(commands);
        this.description = description;
    }

    @Override
    public void execute() throws UserNotFoundException {
        executedCommands.clear();

        for (Command command : commands) {
            try {
                command.execute();
                executedCommands.add(command);
            } catch (UserNotFoundException e) {
                // Якщо команда не вдалася, скасовуємо всі попередні
                undoExecutedCommands();
                throw e;
            }
        }

        System.out.println("MacroCommand виконана: " + description);
    }

    @Override
    public void undo() throws UserNotFoundException {
        undoExecutedCommands();
        System.out.println("MacroCommand скасована: " + description);
    }

    private void undoExecutedCommands() throws UserNotFoundException {
        // Скасовуємо команди в зворотному порядку
        List<Command> reversedCommands = new ArrayList<>(executedCommands);
        Collections.reverse(reversedCommands);

        for (Command command : reversedCommands) {
            command.undo();
        }

        executedCommands.clear();
    }

    @Override
    public String getDescription() {
        return description + " (" + commands.size() + " команд)";
    }

    public List<Command> getCommands() {
        return new ArrayList<>(commands);
    }
}

// src/main/java/com/example/olx/application/command/CommandInvoker.java
package com.example.olx.application.command;

import com.example.olx.domain.exception.UserNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class CommandInvoker {
    private final List<Command> commandHistory = new ArrayList<>();
    private int currentPosition = -1;

    public void executeCommand(Command command) throws UserNotFoundException {
        try {
            // Видаляємо команди після поточної позиції (якщо були скасування)
            if (currentPosition < commandHistory.size() - 1) {
                commandHistory.subList(currentPosition + 1, commandHistory.size()).clear();
            }

            command.execute();
            commandHistory.add(command);
            currentPosition++;

            System.out.println("Виконано: " + command.getDescription());
        } catch (UserNotFoundException e) {
            System.err.println("Помилка виконання команди: " + e.getMessage());
            throw e;
        }
    }

    public void undo() throws UserNotFoundException {
        if (canUndo()) {
            try {
                Command command = commandHistory.get(currentPosition);
                command.undo();
                currentPosition--;
                System.out.println("Скасовано: " + command.getDescription());
            } catch (UserNotFoundException e) {
                System.err.println("Помилка скасування команди: " + e.getMessage());
                throw e;
            }
        } else {
            System.out.println("Немає команд для скасування");
        }
    }

    public void redo() throws UserNotFoundException {
        if (canRedo()) {
            try {
                currentPosition++;
                Command command = commandHistory.get(currentPosition);
                command.execute();
                System.out.println("Повторено: " + command.getDescription());
            } catch (UserNotFoundException e) {
                currentPosition--; // Повертаємо позицію назад при помилці
                System.err.println("Помилка повтору команди: " + e.getMessage());
                throw e;
            }
        } else {
            System.out.println("Немає команд для повтору");
        }
    }

    public boolean canUndo() {
        return currentPosition >= 0;
    }

    public boolean canRedo() {
        return currentPosition < commandHistory.size() - 1;
    }

    public void clearHistory() {
        commandHistory.clear();
        currentPosition = -1;
    }

    public List<String> getCommandHistory() {
        return commandHistory.stream()
                .map(Command::getDescription)
                .toList();
    }
}
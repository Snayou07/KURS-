package com.example.olx.application.command;

import com.example.olx.domain.exception.UserNotFoundException;

/**
 * Базовий інтерфейс для команд без результату
 */
public interface Command {
    void execute() throws UserNotFoundException;
    void undo() throws UserNotFoundException;
    String getDescription();
}
package com.example.olx.application.command;

import com.example.olx.domain.exception.UserNotFoundException;

public interface Command {
    void execute() throws UserNotFoundException;
    void undo() throws UserNotFoundException; // Для можливості скасування
    String getDescription(); // Для логування/історії
}
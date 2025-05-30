package com.example.olx.application.command;

public interface Command {
    void execute();
    void undo(); // Для можливості скасування
    String getDescription(); // Для логування/історії
}
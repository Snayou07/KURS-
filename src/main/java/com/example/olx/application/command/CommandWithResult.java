package com.example.olx.application.command;

/**
 * Інтерфейс для команд з результатом
 * @param <T> тип результату команди
 */
public interface CommandWithResult<T> extends Command {
    T getResult();
}
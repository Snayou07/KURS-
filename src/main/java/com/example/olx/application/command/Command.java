// src/main/java/com/example/olx/application/command/Command.java
package com.example.olx.application.command;

import com.example.olx.domain.exception.UserNotFoundException;

public interface Command {
    void execute() throws UserNotFoundException;
    void undo() throws UserNotFoundException;
    String getDescription();
}
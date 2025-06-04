// src/main/java/com/example/olx/domain/exception/UserNotFoundException.java
package com.example.olx.domain.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
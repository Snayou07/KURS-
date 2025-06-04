// src/main/java/com/example/olx/domain/exception/InvalidInputException.java
package com.example.olx.domain.exception;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
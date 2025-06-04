// src/main/java/com/example/olx/domain/exception/AdNotFoundException.java
package com.example.olx.domain.exception;

public class AdNotFoundException extends RuntimeException {
    public AdNotFoundException(String message) {
        super(message);
    }

    public AdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
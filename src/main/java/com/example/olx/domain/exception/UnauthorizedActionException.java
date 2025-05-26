// com/example/olx/domain/exception/UnauthorizedActionException.java
package com.example.olx.domain.exception;

public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
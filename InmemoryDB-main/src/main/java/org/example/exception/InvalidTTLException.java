package org.example.exception;

public class InvalidTTLException extends RuntimeException {
    public InvalidTTLException(String message) {
        super(message);
    }
}

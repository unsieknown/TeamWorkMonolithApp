package com.mordiniaa.backend.exceptions;

public class SessionIntegrityException extends RuntimeException {
    public SessionIntegrityException(String message) {
        super(message);
    }
}

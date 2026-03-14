package com.mordiniaa.backend.exceptions;

public class UnsupportedOperationException extends RuntimeException {

    public UnsupportedOperationException() {
        this("Unsupported Operation");
    }

    public UnsupportedOperationException(String message) {
        super(message);
    }
}

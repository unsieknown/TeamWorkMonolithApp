package com.mordiniaa.backend.exceptions;

public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundException() {
        super("Board Not Found");
    }

    public BoardNotFoundException(String message) {
        super(message);
    }
}

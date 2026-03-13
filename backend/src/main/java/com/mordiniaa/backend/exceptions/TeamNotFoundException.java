package com.mordiniaa.backend.exceptions;

public class TeamNotFoundException extends RuntimeException {

    public TeamNotFoundException() {
        this("Team Not Found");
    }

    public TeamNotFoundException(String message) {
        super(message);
    }
}

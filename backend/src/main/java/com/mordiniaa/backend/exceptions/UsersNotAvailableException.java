package com.mordiniaa.backend.exceptions;

public class UsersNotAvailableException extends RuntimeException {

    public UsersNotAvailableException() {
        super("One Or More Users May Not Be Available");
    }

    public UsersNotAvailableException(String message) {
        super(message);
    }
}

package com.mordiniaa.backend.exceptions;

public class ContactDataValidationException extends RuntimeException {
    public ContactDataValidationException(String message) {
        super(message);
    }
}

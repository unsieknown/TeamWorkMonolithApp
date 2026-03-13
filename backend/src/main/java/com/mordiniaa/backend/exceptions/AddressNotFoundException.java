package com.mordiniaa.backend.exceptions;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException() {
        this("Address Not Found");
    }

    public AddressNotFoundException(String message) {
        super(message);
    }
}

package com.mordiniaa.backend.exceptions;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException() {
        this("Role Not Found Exception");
    }

    public RoleNotFoundException(String message) {
        super(message);
    }
}

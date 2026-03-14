package com.mordiniaa.backend.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnexpectedException extends RuntimeException {

    private int status;

    public UnexpectedException(String message, int status) {
        super(message);
        this.status = status;
    }

    public UnexpectedException(String message) {
        super(message);
        status = 500;
    }
}

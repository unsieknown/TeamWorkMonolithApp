package com.mordiniaa.backend.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArgumentNotPresentException extends RuntimeException {

    private int status;

    public ArgumentNotPresentException(String message, int status) {
        super(message);
        this.status = status;
    }

    public ArgumentNotPresentException(String message) {
        super(message);
        status = 400;
    }
}

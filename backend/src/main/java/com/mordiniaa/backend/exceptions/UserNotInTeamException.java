package com.mordiniaa.backend.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNotInTeamException extends RuntimeException {

    private int status;

    public UserNotInTeamException(String message, int status) {
        super(message);
        this.status = status;
    }

    public UserNotInTeamException(String message) {
        super(message);
        this.status = 404;
    }
}

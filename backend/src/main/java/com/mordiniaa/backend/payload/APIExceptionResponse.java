package com.mordiniaa.backend.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class APIExceptionResponse {

    private int status;
    private String message;
    private Instant timestamp = Instant.now();

    public APIExceptionResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}

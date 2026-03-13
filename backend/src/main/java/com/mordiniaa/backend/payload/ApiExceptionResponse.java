package com.mordiniaa.backend.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class ApiExceptionResponse {

    private int status;
    private String message;
    private Instant timestamp = Instant.now();

    public ApiExceptionResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}

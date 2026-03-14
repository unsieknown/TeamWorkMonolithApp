package com.mordiniaa.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Schema(name = "Api Exception Response", description = "Schema to hold error response information")
public class APIExceptionResponse {

    @Schema(name = "status", description = "Response Status Code")
    private int status;

    @Schema(name = "message", description = "Response Message")
    private String message;

    @Schema(name = "timestamp", description = "Error Date")
    private Instant timestamp = Instant.now();

    public APIExceptionResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}

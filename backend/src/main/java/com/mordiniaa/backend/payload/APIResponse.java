package com.mordiniaa.backend.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "Api Response",
        description = "Generic Box For Response Data"
)
public class APIResponse<T> {

    @Schema(name = "message", description = "Response Message")
    private String message;

    @Schema(name = "data", description = "Generic Field Storing Retrieved Data")
    private T data;
}

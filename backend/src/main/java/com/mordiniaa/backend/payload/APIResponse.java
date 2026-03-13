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

    private String message;
    private T data;
}

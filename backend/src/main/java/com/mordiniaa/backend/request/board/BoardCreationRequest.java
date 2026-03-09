package com.mordiniaa.backend.request.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BoardCreationRequest {

    @NotNull
    private UUID teamId;

    @NotBlank
    @Size(min = 5, max = 40)
    @Pattern(regexp = "^\\p{L}+([ -]\\p{L}+)*$")
    private String boardName;
}

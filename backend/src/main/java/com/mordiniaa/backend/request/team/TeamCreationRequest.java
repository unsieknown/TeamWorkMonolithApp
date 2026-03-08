package com.mordiniaa.backend.request.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class TeamCreationRequest {

    @NotBlank
    @Size(min = 5, max = 40)
    @Pattern(regexp = "^\\p{L}+([ -]\\p{L}+)*$")
    private String teamName;
}

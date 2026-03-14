package com.mordiniaa.backend.request.user.patch;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PatchUserDataRequest {

    @Pattern(regexp = "^[A-Z][a-z]+$")
    private String firstname;

    @Pattern(regexp = "^[A-Z][a-z]+$")
    private String lastname;
}

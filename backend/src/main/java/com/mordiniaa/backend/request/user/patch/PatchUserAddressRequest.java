package com.mordiniaa.backend.request.user.patch;

import com.mordiniaa.backend.request.user.AddressRequest;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PatchUserAddressRequest implements AddressRequest {

    @Pattern(regexp = "^[A-Za-zĄĆĘŁŃÓŚŹŻąćęłńóśźż0-9 .'-]+$")
    @Size(min = 5, max = 40)
    private String street;

    @Pattern(regexp = "^[A-Z][a-z]+$")
    @Size(min = 2, max = 30)
    private String city;

    @Pattern(regexp = "^[A-Z][a-z]+$")
    @Size(min = 2, max = 30)
    private String country;

    @Pattern(regexp = "\\d{2}-\\d{3}")
    private String zipCode;

    @Pattern(regexp = "^[A-Z][a-z]+$")
    @Size(min = 2, max = 20)
    private String district;
}

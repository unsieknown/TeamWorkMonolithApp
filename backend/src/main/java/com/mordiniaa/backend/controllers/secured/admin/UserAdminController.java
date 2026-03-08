package com.mordiniaa.backend.controllers.secured.admin;

import com.mordiniaa.backend.dto.user.UserDto;
import com.mordiniaa.backend.request.user.CreateUserRequest;
import com.mordiniaa.backend.request.user.PasswordRequest;
import com.mordiniaa.backend.request.user.patch.PatchUserAddressRequest;
import com.mordiniaa.backend.request.user.patch.PatchUserContactDataRequest;
import com.mordiniaa.backend.request.user.patch.PatchUserDataRequest;
import com.mordiniaa.backend.services.user.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/user")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @PostMapping("/create-user")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        UserDto dto = userAdminService.createUser(createUserRequest);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping("/user-pass")
    public ResponseEntity<Void> setUserPassword(
            @RequestParam(name = "u") UUID userId,
            @Valid @RequestBody PasswordRequest passwordRequest
    ) {
        userAdminService.setUserPassword(userId, passwordRequest);
        return ResponseEntity.ok().build();
    }

    public void updateUserBasicData(UUID userId, @Valid @RequestBody PatchUserDataRequest patchUserDataRequest) {

    }

    public void updateUserAddressData(UUID userId, Long addressId, @Valid @RequestBody PatchUserAddressRequest patchUserAddressRequest) {

    }

    public void updateUserContactData(UUID userId, Long contactId, @Valid @RequestBody PatchUserContactDataRequest patchUserContactDataRequest) {

    }

    public void deactivateUser(UUID userId) {

    }
}

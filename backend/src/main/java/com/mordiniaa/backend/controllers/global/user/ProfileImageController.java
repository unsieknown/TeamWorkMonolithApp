package com.mordiniaa.backend.controllers.global.user;

import com.mordiniaa.backend.payload.ApiResponse;
import com.mordiniaa.backend.security.utils.AuthUtils;
import com.mordiniaa.backend.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/image")
public class ProfileImageController {

    private final AuthUtils authUtils;
    private final UserService userService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Void>> addProgileImage(@RequestBody MultipartFile profileImage) {

        UUID userId = authUtils.authenticatedUserId();
        userService.addProfileImage(userId, profileImage);
        return new ResponseEntity<>(
                new ApiResponse<>(
                        "Added Image Successfully",
                        null
                ),
                HttpStatus.CREATED
        );
    }
}

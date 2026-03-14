package com.mordiniaa.backend.controllers.open.authControllers;

import com.mordiniaa.backend.payload.APIResponse;
import com.mordiniaa.backend.request.auth.LoginRequest;
import com.mordiniaa.backend.request.auth.ResetPasswordTokenRequest;
import com.mordiniaa.backend.response.user.UserInfoResponse;
import com.mordiniaa.backend.services.auth.AuthService;
import com.mordiniaa.backend.services.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid Credentials");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpHeaders headers = authService.authenticate(authentication, request)
                .stream()
                .collect(
                        HttpHeaders::new,
                        (h, c) -> h.add(HttpHeaders.SET_COOKIE, c.toString()),
                        HttpHeaders::addAll
                );

        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserDetails() {
        UserInfoResponse infoResponse = authService.getUserDetails(UUID.randomUUID());
        return ResponseEntity.ok(infoResponse);
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signOut(HttpServletRequest request) {
        HttpHeaders headers = authService.logout(request)
                .stream()
                .collect(
                        HttpHeaders::new,
                        (h, c) -> h.add(HttpHeaders.SET_COOKIE, c.toString()),
                        HttpHeaders::addAll
                );
        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshTokens(HttpServletRequest request) {

        HttpHeaders headers = authService.refresh(request)
                .stream()
                .collect(
                        HttpHeaders::new,
                        (h, c) -> h.add(HttpHeaders.SET_COOKIE, c.toString()),
                        HttpHeaders::addAll
                );

        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<APIResponse<Void>> forgotPassword(
            @RequestBody String username
    ) {

        try {
            userService.generatePasswordResetToken(username);
        } catch (Exception e) {
            // ignore
        }

        return ResponseEntity.ok(
                new APIResponse<>(
                        "If the email address exists in our system, a password reset token will be sent to it shortly.",
                        null
                )
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<APIResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordTokenRequest request
    ) {
        userService.resetPassword(request);
        return ResponseEntity.ok(
                new APIResponse<>(
                        "Password Changed Successfully",
                        null
                )
        );
    }
}

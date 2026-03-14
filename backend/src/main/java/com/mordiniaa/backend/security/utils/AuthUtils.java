package com.mordiniaa.backend.security.utils;

import com.mordiniaa.backend.security.objects.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    public UUID authenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return principal.userId();
    }
}

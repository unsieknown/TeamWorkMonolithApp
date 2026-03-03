package com.mordiniaa.backend.services.auth;

import com.mordiniaa.backend.models.user.mysql.User;
import com.mordiniaa.backend.response.user.UserInfoResponse;
import com.mordiniaa.backend.security.service.JwtService;
import com.mordiniaa.backend.security.service.token.RefreshTokenService;
import com.mordiniaa.backend.security.service.token.TokenService;
import com.mordiniaa.backend.security.service.user.SecurityUser;
import com.mordiniaa.backend.security.token.Token;
import com.mordiniaa.backend.security.token.TokenSet;
import com.mordiniaa.backend.security.utils.JwtUtils;
import com.mordiniaa.backend.services.user.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserInfoResponse getUserDetails(UUID userId) {

        User user = userService.getUser(userId);
        List<String> roles = List.of(user.getRole().getAppRole().toString()); // TODO: Change In Security

        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getContact().getEmail())
                .accountNonLocked(user.isAccountNonLocked())
                .accountNonExpired(user.isAccountNonExpired())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .deleted(user.isDeleted())
                .credentialsExpiryDate(user.getCredentialsExpiryDate())
                .accountExpiryDate(user.getAccountExpiryDate())
                .roles(roles)
                .build();
    }

    public List<ResponseCookie> authenticate(Authentication authentication, HttpServletRequest request) {

        SecurityUser user = (SecurityUser) authentication.getPrincipal();

        List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        TokenSet tokenSet = tokenService.issue(user.getUserId(), roles, request);

        return createCookieResponse(
                tokenSet.getJwtToken(),
                tokenSet.getRefreshToken()
        );
    }

    public List<ResponseCookie> refresh(HttpServletRequest request) {

        String jwtToken = jwtService.parseJwtTokenFromCookie(request.getCookies());
        String refreshToken = refreshTokenService.parseRefreshTokenFromCookie(request.getCookies());

        Claims claims = jwtService.parseAllowExpired(jwtToken);

        if (jwtToken == null || refreshToken == null) {
            throw new RuntimeException(); // TODO: Change In Exceptions Section
        }

        UUID userId = jwtService.extractUserId(claims);
        UUID sessionId = jwtService.extractSessionId(claims);

        TokenSet tokenSet = tokenService.refreshToken(
                userId,
                sessionId,
                refreshToken,
                request
        );
        
        return createCookieResponse(
                tokenSet.getJwtToken(),
                tokenSet.getRefreshToken()
        );
    }

    private List<ResponseCookie> createCookieResponse(Token... tokens) {

        List<ResponseCookie> cookies = new ArrayList<>();
        for (Token token : tokens) {
            ResponseCookie cookie = jwtUtils.getCookie(
                    token.getTokenName(),
                    token.getToken(),
                    Duration.ofMillis(token.getTtl())
            );
            cookies.add(cookie);
        }
        return cookies;
    }
}

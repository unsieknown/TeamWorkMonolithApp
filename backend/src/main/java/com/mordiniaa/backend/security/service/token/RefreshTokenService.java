package com.mordiniaa.backend.security.service.token;

import com.mordiniaa.backend.events.authentication.events.DeactivateSessionAndTokensEvent;
import com.mordiniaa.backend.events.authentication.events.SessionMisuseEvent;
import com.mordiniaa.backend.models.Session;
import com.mordiniaa.backend.security.model.RefreshTokenFamily;
import com.mordiniaa.backend.security.token.RefreshToken;
import com.mordiniaa.backend.security.model.RefreshTokenEntity;
import com.mordiniaa.backend.repositories.mysql.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenFamilyService refreshTokenFamilyService;
    @Value("${security.app.refresh-token.token-name}")
    private String tokenName;

    @Getter
    @Value("${security.app.refresh-token.validity-days}")
    private int validityDays;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshTokenEntity generateRefreshTokenEntity(RefreshTokenFamily family, String rawToken, List<String> roles) {
        Instant now = Instant.now();
        RefreshTokenEntity token = buildRefreshToken(family, now, null, rawToken, roles);
        return refreshTokenRepository.save(token);
    }

    @Transactional
    public RefreshTokenEntity rotate(UUID userId, Long tokenId, String oldRawToken, String newRawToken, List<String> roles, HttpServletRequest request) {

        RefreshTokenEntity storedTokenEntity = getRefreshToken(tokenId);
        RefreshTokenFamily family = storedTokenEntity.getRefreshTokenFamily();
        Session userSession = family.getSession();

        try {
            validateSession(userSession, request);
        } catch (Exception e) {
            applicationEventPublisher.publishEvent(
                    new SessionMisuseEvent(userId)
            );
            throw e;
        }

        Instant now = Instant.now();
        boolean tokenExpired = storedTokenEntity.getExpiresAt().isBefore(now);

        if (storedTokenEntity.isRevoked() || tokenExpired) {
            applicationEventPublisher.publishEvent(
                    new DeactivateSessionAndTokensEvent(family.getId(), userSession.getSessionId(), now)
            );
            throw new RuntimeException(); // TODO: Change In Exceptions Section
        }

        if (!Objects.equals(family.getUserId(), userId))
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        boolean familyExpired = family.getExpiresAt().isBefore(now);
        if (family.isRevoked() || familyExpired) {
            applicationEventPublisher.publishEvent(
                    new DeactivateSessionAndTokensEvent(family.getId(), userSession.getSessionId(), now)
            );
            throw new RuntimeException(); // TODO: Change In Exceptions Section
        }

        if (!validateTokensMatch(oldRawToken, storedTokenEntity.getHashedToken())) {
            log.info("Invalid refresh token");
            throw new RuntimeException();
        }

        RefreshTokenEntity newTokenEntity = buildRefreshToken(family, now, storedTokenEntity.getId(), newRawToken, roles);
        RefreshTokenEntity savedEntity = refreshTokenRepository.save(newTokenEntity);

        rotateToken(savedEntity.getId(), storedTokenEntity.getId(), now);
        return savedEntity;
    }

    private void validateSession(Session userSession, HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");
        if (!Objects.equals(userSession.getUserAgent(), userAgent))
            throw new RuntimeException(); // TODO: Change In Exceptions Section
    }

    public RefreshToken generateRefreshToken(RefreshTokenEntity entity, String rawToken) {
        String storedRawToken = entity.getId() + "." + rawToken;
        return new RefreshToken(tokenName, storedRawToken, entity.getExpiresAt().toEpochMilli()-Instant.now().toEpochMilli());
    }

    public RefreshTokenEntity getRefreshToken(Long tokenId) {
        return refreshTokenRepository.findById(tokenId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section
    }

    @Transactional
    public void rotateToken(Long newTokenId, Long oldTokenId, Instant revokedAt) {
        refreshTokenRepository.rotateToken(newTokenId, oldTokenId, revokedAt);
    }

    private byte[] sha256Bytes(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private RefreshTokenEntity buildRefreshToken(RefreshTokenFamily family, Instant time, Long parentId, String rawToken, List<String> roles) {

        String hashed = Base64.getUrlEncoder().withoutPadding().encodeToString(sha256Bytes(rawToken));
        return RefreshTokenEntity.builder()
                .hashedToken(hashed)
                .refreshTokenFamily(family)
                .parentId(parentId)
                .roles(roles)
                .createdAt(time)
                .expiresAt(time.plus(Duration.ofDays(validityDays)))
                .build();
    }

    public String parseRefreshTokenFromCookie(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(tokenName))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
    }

    public ResponseCookie clearUserToken() {
        return ResponseCookie.from(tokenName).path("/").build();
    }

    public Long getStoredRefreshTokenForSession(UUID sessionId) {
        return refreshTokenRepository.findTokenIdBySessionId(sessionId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section
    }

    @Transactional
    public RefreshToken deactivateRefreshToken(UUID userId, UUID sessionId, Long idPart, String tokenPart) {

        RefreshTokenEntity entity = refreshTokenRepository.findRefreshTokenEntityByIdAndRefreshTokenFamily_UserId(idPart, userId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section

        RefreshTokenFamily family = entity.getRefreshTokenFamily();
        if (!Objects.equals(family.getUserId(), userId)) {
            throw new RuntimeException(); // TODO: Change In Exceptions Section
        }

        Session session = family.getSession();
        if (!Objects.equals(session.getSessionId(), sessionId))
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        if (!validateTokensMatch(tokenPart, entity.getHashedToken())) {
            throw new RuntimeException(); // TODO: Change In Exceptions Section
        }

        refreshTokenFamilyService.deactivateUserAuthenticationFamily(family.getId(), Instant.now());

        return new RefreshToken(tokenName, "", 0);
    }

    public boolean validateTokensMatch(String rawToken, String storedToken) {
        return MessageDigest.isEqual(
                sha256Bytes(rawToken),
                Base64.getUrlDecoder().decode(storedToken)
        );
    }
}

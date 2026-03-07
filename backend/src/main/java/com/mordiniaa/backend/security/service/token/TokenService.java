package com.mordiniaa.backend.security.service.token;

import com.mordiniaa.backend.models.Session;
import com.mordiniaa.backend.models.user.mysql.User;
import com.mordiniaa.backend.security.model.RefreshTokenEntity;
import com.mordiniaa.backend.security.model.RefreshTokenFamily;
import com.mordiniaa.backend.security.service.JwtService;
import com.mordiniaa.backend.security.service.SessionRedisService;
import com.mordiniaa.backend.security.token.JwtToken;
import com.mordiniaa.backend.security.token.RefreshToken;
import com.mordiniaa.backend.security.token.TokenSet;
import com.mordiniaa.backend.services.auth.SessionService;
import com.mordiniaa.backend.services.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RawTokenService rawTokenService;
    private final RefreshTokenService refreshTokenService;
    private final SessionRedisService sessionRedisService;
    private final JwtService jwtService;
    private final UserService userService;
    private final SessionService sessionService;
    private final RefreshTokenFamilyService refreshTokenFamilyService;

    @Transactional
    public TokenSet issue(UUID userId, List<String> roles, HttpServletRequest request) {

        String rawToken = rawTokenService.generateOpaqueToken();

        Session session = sessionService.createSession(request);
        RefreshTokenFamily family = refreshTokenFamilyService.createNewFamily(userId, session);
        family.setSession(session);

        RefreshTokenEntity entity = refreshTokenService.generateRefreshTokenEntity(
                family,
                rawToken,
                roles
        );

        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(entity, rawToken);

        Long ttl = Duration.between(Instant.now(), entity.getExpiresAt()).toMillis();
        sessionRedisService.storeSession(
                session.getSessionId(),
                entity.getId(),
                ttl
        );

        JwtToken jwtToken = jwtService.buildJwt(
                userId.toString(),
                session.getSessionId().toString(),
                roles
        );

        return new TokenSet(jwtToken, refreshToken);
    }

    @Transactional
    public TokenSet refreshToken(UUID userId, UUID sessionId, String oldRefreshToken, HttpServletRequest request) {

        int dotIdx = oldRefreshToken.indexOf(".");
        if (dotIdx < 1) throw new RuntimeException(); // TODO: Change in exceptions Section

        String idPart = oldRefreshToken.substring(0, dotIdx);
        String tokenPart = oldRefreshToken.substring(dotIdx + 1);

        long tokenId;
        try {
            tokenId = Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            throw new RuntimeException(); // TODO: Change In Exceptions Section
        }

        Long storedTokenId = sessionRedisService.getTokenIdBySessionId(sessionId);
        if (storedTokenId == null) {
            storedTokenId = refreshTokenService.getStoredRefreshTokenForSession(sessionId)
                    .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section
        }

        if (tokenId != storedTokenId)
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        User user = userService.getUser(userId);
        List<String> roles = List.of(user.getRole().getAppRole().toString());

        String newRawToken = rawTokenService.generateOpaqueToken();
        RefreshTokenEntity storedEntity;
        try {
            storedEntity = refreshTokenService.rotate(userId, tokenId, tokenPart, newRawToken, roles, request);
        } catch (Exception e) {
            sessionRedisService.deleteSession(sessionId);
            throw new RuntimeException(); // TODO: Change In Exceptions Section
        }

        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(storedEntity, newRawToken);

        Long ttl = Duration.between(Instant.now(), storedEntity.getExpiresAt()).toMillis();
        sessionRedisService.rotateRefreshToken(
                sessionId,
                storedEntity.getId(),
                ttl
        );

        JwtToken jwtToken = jwtService.buildJwt(
                userId.toString(),
                sessionId.toString(),
                roles
        );

        return new TokenSet(jwtToken, refreshToken);
    }
}

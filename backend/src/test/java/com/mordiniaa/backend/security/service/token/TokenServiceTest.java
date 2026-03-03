package com.mordiniaa.backend.security.service.token;

import com.mordiniaa.backend.models.user.mysql.AppRole;
import com.mordiniaa.backend.repositories.mysql.RefreshTokenFamilyRepository;
import com.mordiniaa.backend.repositories.mysql.RefreshTokenRepository;
import com.mordiniaa.backend.repositories.mysql.SessionRepository;
import com.mordiniaa.backend.repositories.mysql.UserRepository;
import com.mordiniaa.backend.security.service.SessionRedisService;
import com.mordiniaa.backend.security.token.JwtToken;
import com.mordiniaa.backend.security.token.RefreshToken;
import com.mordiniaa.backend.security.token.TokenSet;
import com.mordiniaa.backend.security.utils.JwtUtils;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RefreshTokenFamilyRepository refreshTokenFamilyRepository;

    @Autowired
    private SessionRedisService sessionRedisService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RawTokenService rawTokenService;
    @Autowired
    private SessionRepository sessionRepository;

    @AfterEach
    void tearDown() {
        refreshTokenFamilyRepository.deleteAll();
        sessionRepository.deleteAll();

        ScanOptions options = ScanOptions.scanOptions()
                .match("session:*")
                .build();

        assertNotNull(stringRedisTemplate.getConnectionFactory());
        Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options);
        List<String> keysToDelete = new ArrayList<>();
        while (cursor.hasNext()) {
            keysToDelete.add(new String(cursor.next(), StandardCharsets.UTF_8));
        }
        if (!keysToDelete.isEmpty())
            stringRedisTemplate.delete(keysToDelete);
    }

    @Test
    @DisplayName("Issue Token Test")
    void issueTokenTest() {

        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("ROLE_USER");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.setRemoteAddr("192.168.1.10");
        TokenSet tokenSet = tokenService.issue(userId, roles, request);
        assertNotNull(tokenSet);

        JwtToken jwtToken = tokenSet.getJwtToken();
        RefreshToken refreshToken = tokenSet.getRefreshToken();

        assertNotNull(jwtToken);
        assertNotNull(refreshToken);

        assertNotNull(jwtToken.getToken());
        assertNotNull(jwtToken.getTokenName());
        assertTrue(jwtToken.getTtl() > 1);

        assertNotNull(refreshToken.getToken());
        assertNotNull(refreshToken.getTokenName());
        assertTrue(refreshToken.getTtl() > 1);

        String refreshTokenString = refreshToken.getToken();
        int idx = refreshTokenString.indexOf(".");
        String idPart = refreshTokenString.substring(0, idx);

        UUID sessionId = getSessionIdFromJwtToken(jwtToken.getToken());
        assertNotNull(sessionId);

        Long tokenIdFromRedis = sessionRedisService.getTokenIdBySessionId(sessionId);
        assertNotNull(tokenIdFromRedis);
        assertEquals(Long.parseLong(idPart), tokenIdFromRedis);
    }

    @Test
    @DisplayName("Refresh Token Test")
    void refreshTokenTest() {

        UUID userId = userRepository.findUserByRole_AppRole(AppRole.ROLE_ADMIN)
                .orElseThrow()
                .getUserId();
        List<String> roles = List.of("ROLE_USER");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.setRemoteAddr("192.168.1.10");
        TokenSet tokenSet = tokenService.issue(userId, roles, request);
        UUID sessionId = getSessionIdFromJwtToken(tokenSet.getJwtToken().getToken());

        TokenSet newSet = tokenService.refreshToken(userId, sessionId, tokenSet.getRefreshToken().getToken(), request);
        assertNotNull(newSet);

        JwtToken jwtToken = newSet.getJwtToken();
        RefreshToken refreshToken = newSet.getRefreshToken();

        assertNotNull(jwtToken);
        assertNotNull(refreshToken);

        assertNotNull(jwtToken.getToken());
        assertNotNull(jwtToken.getTokenName());
        assertTrue(jwtToken.getTtl() > 1);

        assertNotNull(refreshToken.getToken());
        assertNotNull(refreshToken.getTokenName());
        assertTrue(refreshToken.getTtl() > 1);

        String refreshTokenString = refreshToken.getToken();
        int idx = refreshTokenString.indexOf(".");
        String idPart = refreshTokenString.substring(0, idx);

        sessionId = getSessionIdFromJwtToken(jwtToken.getToken());
        assertNotNull(sessionId);

        Long tokenIdFromRedis = sessionRedisService.getTokenIdBySessionId(sessionId);
        assertNotNull(tokenIdFromRedis);
        assertEquals(Long.parseLong(idPart), tokenIdFromRedis);
    }

    @Test
    @DisplayName("Refresh Token User Not Found Test")
    void refreshTokenUserNotFoundTest() {

        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("ROLE_USER");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.setRemoteAddr("192.168.1.10");
        TokenSet tokenSet = tokenService.issue(userId, roles, request);
        UUID sessionId = getSessionIdFromJwtToken(tokenSet.getJwtToken().getToken());

        assertThrows(RuntimeException.class,
                () -> tokenService.refreshToken(userId, sessionId, tokenSet.getRefreshToken().getToken(), request));
    }

    @Test
    @DisplayName("Refresh Token Session Not Found")
    void refreshTokenSessionNotFound() {
        UUID userId = userRepository.findUserByRole_AppRole(AppRole.ROLE_ADMIN)
                .orElseThrow()
                .getUserId();
        List<String> roles = List.of("ROLE_USER");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.setRemoteAddr("192.168.1.10");
        TokenSet tokenSet = tokenService.issue(userId, roles, request);

        assertThrows(RuntimeException.class,
                () -> tokenService.refreshToken(userId, UUID.randomUUID(), tokenSet.getRefreshToken().getToken(), request));
    }

    @Test
    @DisplayName("Refresh Token Invalid Token Test")
    void refreshTokenInvalidTokenTest() {

        UUID userId = userRepository.findUserByRole_AppRole(AppRole.ROLE_ADMIN)
                .orElseThrow()
                .getUserId();

        List<String> roles = List.of("ROLE_USER");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.setRemoteAddr("192.168.1.10");
        TokenSet tokenSet = tokenService.issue(userId, roles, request);
        UUID sessionId = getSessionIdFromJwtToken(tokenSet.getJwtToken().getToken());

        String anotherToken = rawTokenService.generateOpaqueToken();
        assertThrows(RuntimeException.class,
                () -> tokenService.refreshToken(userId, sessionId, anotherToken, request));
    }

    private UUID getSessionIdFromJwtToken(String token) {
        String session = (String) Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("sid");

        try {
            return UUID.fromString(session);
        } catch (Exception e) {
            return null;
        }
    }
}

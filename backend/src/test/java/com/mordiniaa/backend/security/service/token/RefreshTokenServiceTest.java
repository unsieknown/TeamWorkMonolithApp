package com.mordiniaa.backend.security.service.token;

import com.mordiniaa.backend.models.Session;
import com.mordiniaa.backend.repositories.mysql.RefreshTokenFamilyRepository;
import com.mordiniaa.backend.repositories.mysql.RefreshTokenRepository;
import com.mordiniaa.backend.repositories.mysql.SessionRepository;
import com.mordiniaa.backend.security.model.RefreshTokenEntity;
import com.mordiniaa.backend.security.model.RefreshTokenFamily;
import com.mordiniaa.backend.services.auth.SessionService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.*;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenFamilyRepository refreshTokenFamilyRepository;
    @Autowired
    private RawTokenService rawTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private RefreshTokenFamilyService refreshTokenFamilyService;

    @AfterEach
    void tearDown() {
        refreshTokenFamilyRepository.deleteAll();
        sessionRepository.deleteAll();
    }

    @Test
    @DisplayName("Generate Refresh Token Entity Test")
    void generateRefreshTokenEntityTest() {

        UUID userId = UUID.randomUUID();
        String rawToken = rawTokenService.generateOpaqueToken();
        List<String> roles = List.of("ROLE_ADMIN");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");
        Session session = sessionService.createSession(request);
        RefreshTokenFamily family = refreshTokenFamilyService.createNewFamily(userId, session);
        RefreshTokenEntity entity = refreshTokenService.generateRefreshTokenEntity(
                family,
                rawToken,
                roles
        );

        assertNotNull(entity);
        assertFalse(entity.isRevoked());
        assertTrue(entity.getExpiresAt().toEpochMilli() > Instant.now().toEpochMilli());
    }

    @Test
    @DisplayName("Rotate Token Valid Test")
    void rotateTokenValidTest() {

        UUID userId = UUID.randomUUID();
        Long familyId = new Random().nextLong();
        String rawToken = rawTokenService.generateOpaqueToken();
        List<String> roles = List.of("ROLE_ADMIN");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");
        Session session = sessionService.createSession(request);
        RefreshTokenFamily family = refreshTokenFamilyService.createNewFamily(userId, session);
        RefreshTokenEntity entity = refreshTokenService.generateRefreshTokenEntity(
                family,
                rawToken,
                roles
        );

        Long savedFamilyId = entity.getRefreshTokenFamily().getId();

        Long tokenId = entity.getId();
        String newToken = rawTokenService.generateOpaqueToken();

        RefreshTokenEntity rotatedEntity = refreshTokenService.rotate(userId, tokenId, rawToken, newToken, roles, request);
        assertNotNull(rotatedEntity);
        assertEquals(roles, rotatedEntity.getRoles());

        RefreshTokenEntity revokedEntity = refreshTokenRepository.findById(tokenId)
                .orElseThrow();
        assertTrue(revokedEntity.isRevoked());
        assertNotNull(revokedEntity.getRevokedAt());

        assertEquals(savedFamilyId, rotatedEntity.getRefreshTokenFamily().getId());
    }

    @Test
    @DisplayName("Rotate Refresh Token Token Not Found Test")
    void rotateRefreshTokenTokenNotFoundTest() {

        UUID userId = UUID.randomUUID();
        Long tokenId = new Random().nextLong();
        String oldRawToken = rawTokenService.generateOpaqueToken();
        String newRawToken = rawTokenService.generateOpaqueToken();
        List<String> roles = List.of("ROLE_USER");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");

        assertThrows(RuntimeException.class,
                () -> refreshTokenService.rotate(userId, tokenId, oldRawToken, newRawToken, roles, request));
    }

    @Test
    @DisplayName("Rotate Token Revoked Test")
    void rotateTokenTokenRevokedTest() {

        UUID userId = UUID.randomUUID();
        Long familyId = new Random().nextLong();
        String rawToken = rawTokenService.generateOpaqueToken();
        List<String> roles = List.of("ROLE_USER");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");
        Session session = sessionService.createSession(request);
        RefreshTokenFamily family = refreshTokenFamilyService.createNewFamily(userId, session);
        RefreshTokenEntity entity = refreshTokenService.generateRefreshTokenEntity(
                family,
                rawToken,
                roles
        );

        entity.setRevoked(true);
        refreshTokenRepository.save(entity);

        String newToken = rawTokenService.generateOpaqueToken();
        assertThrows(RuntimeException.class,
                () -> refreshTokenService.rotate(userId, entity.getId(), rawToken, newToken, roles, request));
    }

    @Test
    @DisplayName("Rotate Token Raw Tokens Mismatch Test")
    void rotateTokenRawTokenMismatchTest() {
        UUID userId = UUID.randomUUID();
        String rawToken = rawTokenService.generateOpaqueToken();
        List<String> roles = List.of("ROLE_USER");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");
        Session session = sessionService.createSession(request);
        RefreshTokenFamily family = refreshTokenFamilyService.createNewFamily(userId, session);
        RefreshTokenEntity entity = refreshTokenService.generateRefreshTokenEntity(
                family,
                rawToken,
                roles
        );

        Long tokenId = entity.getId();
        String newRawToken = rawTokenService.generateOpaqueToken();
        String newRawToke2 = rawTokenService.generateOpaqueToken();

        assertThrows(RuntimeException.class,
                () -> refreshTokenService.rotate(
                        userId,
                        tokenId,
                        newRawToken,
                        newRawToke2,
                        roles,
                        request
                ));
    }
}

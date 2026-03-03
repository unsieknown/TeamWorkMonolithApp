package com.mordiniaa.backend.security.service.token;

import com.mordiniaa.backend.repositories.mysql.RefreshTokenFamilyRepository;
import com.mordiniaa.backend.repositories.mysql.SessionRepository;
import com.mordiniaa.backend.security.model.RefreshTokenFamily;
import com.mordiniaa.backend.services.auth.SessionService;
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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RefreshTokenFamilyServiceTest {

    @Autowired
    private RefreshTokenFamilyService refreshTokenFamilyService;

    @Autowired
    private RefreshTokenFamilyRepository refreshTokenFamilyRepository;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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
    @DisplayName("Get Refresh Token Family Or Create Test")
    void createNewFamilyTest() {

        UUID userId = UUID.randomUUID();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.setRemoteAddr("192.168.1.10");
        RefreshTokenFamily newFamily = refreshTokenFamilyService.createNewFamily(userId, sessionService.createSession(request));
        assertNotNull(newFamily);

        assertEquals(userId, newFamily.getUserId());

        Long savedFamilyId = newFamily.getId();
        assertTrue(savedFamilyId > 1);

        RefreshTokenFamily savedFamily = refreshTokenFamilyService.createNewFamily(userId, sessionService.createSession(request));
        assertNotNull(savedFamily);
        assertEquals(userId, savedFamily.getUserId());

        assertTrue(savedFamily.getExpiresAt().toEpochMilli() > Instant.now().plus(Duration.ofDays(89)).toEpochMilli());
    }
}


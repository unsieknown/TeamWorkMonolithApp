package com.mordiniaa.backend.security.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class SessionRedisServiceTest {

    @Autowired
    private SessionRedisService sessionRedisService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("Create Session Test")
    void storeSessionTest() {

        UUID sessionId = UUID.randomUUID();
        long tokenId = new Random().nextLong();

        sessionRedisService.storeSession(sessionId, tokenId, Duration.ofDays(30).toMillis());

        String val = redisTemplate.opsForValue().get("session:" + sessionId);
        assertNotNull(val);
        assertEquals(tokenId, Long.parseLong(val));
    }

    @Test
    @DisplayName("Rotate Token Test")
    void rotateTokenTest() {

        UUID sessionId = UUID.randomUUID();
        long tokenId = new Random().nextLong();

        sessionRedisService.storeSession(sessionId, tokenId, Duration.ofDays(30).toMillis());

        long newId = new Random().nextLong();
        sessionRedisService.rotateRefreshToken(sessionId, newId, Duration.ofDays(30).toMillis());

        String val = redisTemplate.opsForValue().get("session:" + sessionId);
        assertNotNull(val);
        assertEquals(newId, Long.parseLong(val));
    }

    @Test
    @DisplayName("Delete Session Test")
    void deleteSessionTest() {

        UUID sessionId = UUID.randomUUID();
        long tokenId = new Random().nextLong();

        sessionRedisService.storeSession(sessionId, tokenId, Duration.ofDays(30).toMillis());

        sessionRedisService.deleteSession(sessionId);

        assertNull(sessionRedisService.getTokenIdBySessionId(sessionId));
    }
}

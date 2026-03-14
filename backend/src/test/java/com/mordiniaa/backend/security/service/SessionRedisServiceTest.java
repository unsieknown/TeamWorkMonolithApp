package com.mordiniaa.backend.security.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class SessionRedisServiceTest {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SessionRedisService sessionRedisService;

    @Test
    @DisplayName("Create Session Test")
    void createSessionTest() {

        UUID sessionId = UUID.randomUUID();
        long tokenId = new Random().nextLong();

        sessionRedisService.storeSession(sessionId, tokenId, Instant.now().plusMillis(Duration.ofDays(30).toMillis()).toEpochMilli());

        String val = redisTemplate.opsForValue().get("session:" + sessionId);
        assertNotNull(val);
        assertEquals(tokenId, Long.parseLong(val));
    }

    @Test
    @DisplayName("Rotate Token Test")
    void rotateTokenTest() {

        UUID sessionId = UUID.randomUUID();
        long tokenId = new Random().nextLong();

        sessionRedisService.storeSession(sessionId, tokenId, Instant.now().plusMillis(Duration.ofDays(30).toMillis()).toEpochMilli());

        long newId = new Random().nextLong();
        sessionRedisService.rotateRefreshToken(sessionId, newId, Instant.now().plusMillis(Duration.ofDays(30).toMillis()).toEpochMilli());

        String val = redisTemplate.opsForValue().get("session:" + sessionId);
        assertNotNull(val);
        assertEquals(newId, Long.parseLong(val));
    }

    @Test
    @DisplayName("Delete Session Test")
    void deleteSessionTest() {

        UUID sessionId = UUID.randomUUID();
        long tokenId = new Random().nextLong();

        sessionRedisService.storeSession(sessionId, tokenId, Instant.now().plusMillis(Duration.ofDays(30).toMillis()).toEpochMilli());

        sessionRedisService.deleteSession(sessionId);

        assertThrows(RuntimeException.class,
                () -> sessionRedisService.getTokenIdBySessionId(sessionId));
    }
}
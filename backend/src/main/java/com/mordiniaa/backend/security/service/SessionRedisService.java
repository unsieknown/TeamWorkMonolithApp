package com.mordiniaa.backend.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionRedisService {

    private final StringRedisTemplate redis;

    public void storeSession(UUID sessionId, Long refreshTokenId, Long ttl) {
        redis.opsForValue().set(
                key(sessionId),
                refreshTokenId.toString(),
                Duration.ofMillis(ttl)
        );
    }

    public Long getTokenIdBySessionId(UUID sessionId) {
        return Optional.ofNullable(redis.opsForValue().get(key(sessionId)))
                .map(Long::parseLong)
                .orElse(null);
    }

    public void rotateRefreshToken(UUID sessionId, Long refreshTokenId, Long ttl) {
        redis.opsForValue().set(
                key(sessionId),
                refreshTokenId.toString(),
                Duration.ofMillis(ttl)
        );
    }

    public boolean validateSession(UUID sessionId) {
        return redis.opsForValue().get(key(sessionId)) != null;
    }

    public void deleteSession(UUID sessionId) {
        redis.delete(key(sessionId));
    }

    private String key(UUID sessionId) {
        return "session:".concat(sessionId.toString());
    }
}

package com.mordiniaa.backend.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class RateLimitingRedisService {

    @Value("${redis.keys.rate-limit}")
    private String rtKey;

    private final StringRedisTemplate redisTemplate;

    public Long incRequest(String ip) {

        String key = key(ip);
        Long requests = redisTemplate.opsForValue().increment(key);

        if (requests != null && requests == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        return requests;
    }

    private String key(String ip) {
        return rtKey + ":" + ip;
    }
}

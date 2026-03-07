package com.mordiniaa.backend.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockIpRedisService {

    @Value("${redis.keys.blocked-ip}")
    private String blockedIpKeyName;

    @Value("${redis.keys.ban-count}")
    private String banCount;

    private final FileBanService fileBanService;
    private final StringRedisTemplate redisTemplate;
    private final Cache<String, Boolean> blockedIpCache = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterWrite(Duration.ofSeconds(20))
            .build();

    @PostConstruct
    private void readIps() {
        List<String> ips = fileBanService.loadBannedIps();
        ips.stream().map(String::trim)
                .collect(Collectors.toSet())
                .forEach(ip -> {
                    String key = key(ip);
                    setBlockedIp(ip, key);
                });
    }

    public boolean exists(String ip) {

        Boolean cached = blockedIpCache.getIfPresent(ip);
        if (cached != null)
            return cached;

        String value = redisTemplate.opsForValue().get(key(ip));

        boolean blocked = "permanent".equals(value);

        if (blocked)
            blockedIpCache.put(ip, true);

        return blocked;
    }

    public void blockPermanent(String ip) {

        String key = key(ip);

        String existing = redisTemplate.opsForValue().get(key);
        if ("permanent".equals(existing)) {
            return;
        }

        fileBanService.enqueuePermanentBan(ip);
        setBlockedIp(ip, key);
    }

    private void setBlockedIp(String ip, String key) {
        redisTemplate.opsForValue().set(key, "permanent");
        blockedIpCache.put(ip, true);
    }

    public void blockTemporaryEscalating(String ip) {

        String key = key(ip);

        String existing = redisTemplate.opsForValue().get(key);

        if ("permanent".equals(existing)) {
            return;
        }

        String banCountKey = banCount + ":" + ip;
        Long bans = redisTemplate.opsForValue().increment(banCountKey);

        if (bans == null) {
            bans = 1L;
        }

        if (bans == 1) {
            redisTemplate.expire(banCountKey, Duration.ofHours(24));
        }

        if (bans >= 4) {
            blockPermanent(ip);
            return;
        }

        Duration duration;

        if (bans == 1) {
            duration = Duration.ofMinutes(1);
        } else if (bans == 2) {
            duration = Duration.ofMinutes(5);
        } else {
            duration = Duration.ofMinutes(30);
        }

        redisTemplate.opsForValue().set(key, "temporary", duration);

        blockedIpCache.put(ip, true);
    }

    private String key(String ip) {
        return blockedIpKeyName + ":" + ip;
    }
}

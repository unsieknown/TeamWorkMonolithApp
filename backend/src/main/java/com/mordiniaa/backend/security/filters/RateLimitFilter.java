package com.mordiniaa.backend.security.filters;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mordiniaa.backend.security.service.BlockIpRedisService;
import com.mordiniaa.backend.utils.IpAddrUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.LongAdder;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final IpAddrUtils ipAddrUtils;
    private final BlockIpRedisService blockIpRedisService;

    private final Cache<String, LongAdder> localRate =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofSeconds(5))
                    .maximumSize(50_000)
                    .build();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = ipAddrUtils.extractClientIp(request);

        LongAdder counter = localRate.get(ip, k -> new LongAdder());
        counter.increment();
        long requests = counter.longValue();

        if (requests <= 100) {
            filterChain.doFilter(request, response);
            return;
        }

        if (requests <= 300) {
            response.setStatus(429);
            return;
        }

        if (requests <= 500) {
            response.setStatus(429);
            return;
        }

        blockIpRedisService.blockTemporaryEscalating(ip);
        response.setStatus(429);
    }
}
package com.mordiniaa.backend.security.filters;

import com.mordiniaa.backend.security.service.RateLimitingRedisService;
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

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final IpAddrUtils ipAddrUtils;
    private final RateLimitingRedisService rateLimitingRedisService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String userId = ipAddrUtils.extractClientId(request);

        Long requests = rateLimitingRedisService.incRequest(userId);
        if (requests == null) {
            response.setStatus(500);
            return;
        }

        if (requests > 100) {
            response.setStatus(429);
            return;
        }
        filterChain.doFilter(request, response);
    }
}

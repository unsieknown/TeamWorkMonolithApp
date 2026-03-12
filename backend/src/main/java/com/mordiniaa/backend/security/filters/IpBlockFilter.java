package com.mordiniaa.backend.security.filters;

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

@Component
@RequiredArgsConstructor
public class IpBlockFilter extends OncePerRequestFilter {

    private final IpAddrUtils ipAddrUtils;
    private final BlockIpRedisService blockIpRedisService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = ipAddrUtils.extractClientIp(request);

        if (!blockIpRedisService.exists(ip)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }
}

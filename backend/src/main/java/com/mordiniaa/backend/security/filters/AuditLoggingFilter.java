package com.mordiniaa.backend.security.filters;

import com.mordiniaa.backend.models.audit.AuditEventType;
import com.mordiniaa.backend.audit.logAudit.AuditLogEvent;
import com.mordiniaa.backend.audit.logAudit.kafka.AuditPublisher;
import com.mordiniaa.backend.security.objects.JwtPrincipal;
import com.mordiniaa.backend.utils.IpAddrUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLoggingFilter extends OncePerRequestFilter {

    private final AuditPublisher auditPublisher;
    private final IpAddrUtils ipAddrUtils;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println(request.getRequestURI());
        long start = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            UUID userId = null;
            UUID sessionId = null;

            if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
                userId = principal.userId();
                sessionId = principal.sessionId();
            }

            int status = response.getStatus();
            AuditLogEvent event = AuditLogEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(resolveType(status))
                    .userId(userId)
                    .sessionId(sessionId)
                    .method(request.getMethod())
                    .uri(request.getRequestURI())
                    .status(response.getStatus())
                    .ip(ipAddrUtils.extractClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .duration(duration)
                    .timestamp(Instant.now())
                    .details(null)
                    .build();

            auditPublisher.publish(event);
        }
    }

    private AuditEventType resolveType(int status) {
        return switch (status) {
            case 401 -> AuditEventType.UNAUTHORIZED;
            case 403 -> AuditEventType.ACCESS_DENIED;
            case 429 -> AuditEventType.TO_MANY_REQUESTS;
            default -> AuditEventType.REQUEST;
        };
    }
}
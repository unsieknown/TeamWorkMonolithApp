package com.mordiniaa.backend.audit.logAudit;

import com.mordiniaa.backend.models.audit.AuditEventType;
import lombok.Builder;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Builder
public record AuditLogEvent(
        String eventId,
        AuditEventType eventType,
        UUID userId,
        UUID sessionId,
        String method,
        String uri,
        int status,
        String ip,
        String userAgent,
        long duration,
        Instant timestamp,
        String details
) {
}

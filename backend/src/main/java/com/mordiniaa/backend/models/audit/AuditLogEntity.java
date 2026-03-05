package com.mordiniaa.backend.models.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "AuditLogEntity")
@Table(name = "audit_log_entities")
public class AuditLogEntity {

    @Id
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    private AuditEventType eventType;
    private UUID userId;
    private UUID sessionId;
    private String method;
    private String uri;
    private int status;
    private String ip;
    private String userAgent;
    private long duration;
    private Instant timestamp;
    private String details;
}

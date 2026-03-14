package com.mordiniaa.backend.services.audit;

import com.mordiniaa.backend.audit.logAudit.AuditLogEvent;
import com.mordiniaa.backend.models.audit.AuditLogEntity;
import com.mordiniaa.backend.repositories.mysql.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    public void storeAuditLog(AuditLogEvent event) {

        AuditLogEntity entity = AuditLogEntity
                .builder()
                .eventId(UUID.fromString(event.eventId()))
                .eventType(event.eventType())
                .userId(event.userId())
                .sessionId(event.sessionId())
                .method(event.method())
                .uri(event.uri())
                .status(event.status())
                .ip(event.ip())
                .userAgent(event.userAgent())
                .duration(event.duration())
                .timestamp(event.timestamp())
                .details(event.details())
                .build();
        auditRepository.save(entity);
    }
}

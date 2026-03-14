package com.mordiniaa.backend.audit.logAudit.kafka;

import com.mordiniaa.backend.audit.logAudit.AuditLogEvent;
import com.mordiniaa.backend.services.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditService auditService;

    @KafkaListener(topics = "${spring.kafka.topics.audit.name}", groupId = "${spring.kafka.topics.audit.group-name}")
    public void consumer(AuditLogEvent event) {
        auditService.storeAuditLog(event);
    }
}

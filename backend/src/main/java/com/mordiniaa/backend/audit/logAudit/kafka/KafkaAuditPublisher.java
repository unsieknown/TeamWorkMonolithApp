package com.mordiniaa.backend.audit.logAudit.kafka;

import com.mordiniaa.backend.audit.logAudit.AuditLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaAuditPublisher implements AuditPublisher {

    @Value("${spring.kafka.topics.audit.name}")
    private String auditTopic;

    private final KafkaTemplate<String, AuditLogEvent> kafkaTemplate;

    @Override
    public void publish(AuditLogEvent event) {

        String key = event.sessionId() != null
                ? event.sessionId().toString()
                : "anonymous";

        try {

            kafkaTemplate.send(auditTopic, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                            log.warn("Kafka unavailable, audit event not sent: {}", cause.getMessage());
                        }
                    });

        } catch (Exception ex) {
            log.warn("Kafka unavailable: {}", ex.getMessage());
        }
    }
}

package com.mordiniaa.backend.audit.logAudit.kafka;

import com.mordiniaa.backend.audit.logAudit.AuditLogEvent;

public interface AuditPublisher {
    void publish(AuditLogEvent event);
}

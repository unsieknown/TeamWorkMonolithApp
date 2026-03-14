package com.mordiniaa.backend.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicsConfig {

    @Value("${spring.kafka.topics.audit.name}")
    private String auditTopic;

    @Bean
    public NewTopic createAuditTopic() {
        return new NewTopic(auditTopic, 3, (short) 1);
    }
}

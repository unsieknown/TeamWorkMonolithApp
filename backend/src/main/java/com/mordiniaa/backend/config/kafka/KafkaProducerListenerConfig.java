package com.mordiniaa.backend.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.lang.Nullable;

@Slf4j
@Configuration
public class KafkaProducerListenerConfig {

    @Bean
    public ProducerListener<Object, Object> producerListener() {

        return new ProducerListener<>() {
            @Override
            public void onError(
                    ProducerRecord<Object, Object> producerRecord,
                    @Nullable RecordMetadata recordMetadata,
                    Exception exception
            ) {
                log.warn("Kafka unavailable, event not sent: {}", exception.getMessage());
            }
        };
    }
}

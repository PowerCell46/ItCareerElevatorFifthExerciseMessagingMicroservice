package com.ItCareerElevatorFifthExercise.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, String> persistMessageProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Kafka will attempt the send, then retry up to 10 times, waiting ~200 ms, 400 ms, 800 ms, etc. between attempts
        // (subject to jitter and internal rules), but never longer than 120 seconds total for that record.
        config.put(ProducerConfig.RETRIES_CONFIG, 10);
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 200);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> persistMessageKafkaTemplate() {
        return new KafkaTemplate<>(persistMessageProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, String> userLocationKafkaTemplate() {
        return new KafkaTemplate<>(persistMessageProducerFactory());
    }
}

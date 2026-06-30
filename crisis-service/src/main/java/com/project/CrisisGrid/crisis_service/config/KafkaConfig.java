package com.project.CrisisGrid.crisis_service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.annotation.EnableKafka;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import org.springframework.kafka.listener.ContainerProperties;

import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    // Topic Names
    public static final String CRISIS_CREATED_TOPIC =
            "crisis.created";

    public static final String CRISIS_UPDATED_TOPIC =
            "crisis.updated";

    public static final String CRISIS_RESOLVED_TOPIC =
            "crisis.resolved";

    public static final String RESOURCE_ALLOCATION_TOPIC =
            "resource.allocation";

    /**
     * Producer Configuration
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {

        Map<String, Object> configProps = new HashMap<>();

        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );

        // Reliability
        configProps.put(
                ProducerConfig.ACKS_CONFIG,
                "all"
        );

        configProps.put(
                ProducerConfig.RETRIES_CONFIG,
                3
        );

        configProps.put(
                ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                5
        );

        configProps.put(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                true
        );

        // Performance
        configProps.put(
                ProducerConfig.COMPRESSION_TYPE_CONFIG,
                "snappy"
        );

        configProps.put(
                ProducerConfig.BATCH_SIZE_CONFIG,
                16384
        );

        configProps.put(
                ProducerConfig.LINGER_MS_CONFIG,
                10
        );

        configProps.put(
                ProducerConfig.BUFFER_MEMORY_CONFIG,
                33554432
        );

        return new DefaultKafkaProducerFactory<>(
                configProps
        );
    }

    /**
     * Kafka Template
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {

        return new KafkaTemplate<>(
                producerFactory()
        );
    }

    /**
     * Consumer Configuration
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {

        Map<String, Object> configProps = new HashMap<>();

        configProps.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        configProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                consumerGroupId
        );

        configProps.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        configProps.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class
        );

        configProps.put(
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class.getName()
        );

        // BUG 3 FIX — was `false` (manual commit) but no consumer ever
        // called ack.acknowledge(), so Kafka never received a commit and
        // kept redelivering the same messages forever.
        // Switching to true lets Kafka auto-commit after each poll,
        // which matches the fact that none of the @KafkaListener methods
        // take an Acknowledgment parameter.
        configProps.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                true
        );

        // Auto-commit interval — commit every second (default is 5 s).
        // Keeps the committed offset close to the processed offset so
        // a restart doesn't re-process too many messages.
        configProps.put(
                ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,
                1000
        );

        configProps.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        configProps.put(
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                100
        );

        configProps.put(
                ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
                300000
        );

        // JSON Settings
        configProps.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "*"
        );

        configProps.put(
                JsonDeserializer.USE_TYPE_INFO_HEADERS,
                false
        );

        return new DefaultKafkaConsumerFactory<>(
                configProps
        );
    }

    /**
     * Kafka Listener Container Factory
     *
     * BUG 3 FIX — AckMode was MANUAL_IMMEDIATE, which requires every
     * @KafkaListener method to accept an Acknowledgment parameter and
     * explicitly call ack.acknowledge(). None of the listeners in this
     * project do that, so offsets were never committed and every message
     * was redelivered on restart.
     *
     * Changed to BATCH, which works correctly with auto-commit enabled
     * above. Spring will commit the offset automatically after each
     * poll batch is fully processed, with no changes needed in any
     * listener method.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, Object>
                factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                consumerFactory()
        );

        factory.setConcurrency(3);

        factory.setBatchListener(false);

        // FIXED: was ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.getContainerProperties()
                .setAckMode(
                        ContainerProperties.AckMode.BATCH
                );

        return factory;
    }
}
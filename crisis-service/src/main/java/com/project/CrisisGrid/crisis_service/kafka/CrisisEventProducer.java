package com.project.CrisisGrid.crisis_service.kafka;




import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrisisEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CRISIS_CREATED_TOPIC = "crisis.created";

    private static final String CRISIS_RESOLVED_TOPIC = "crisis.resolved";

    public void publishCrisisCreatedEvent(
            UUID crisisId,
            String title,
            String description) {

        Map<String, Object> event = Map.of(
                "crisisId", crisisId.toString(),
                "title", title,
                "description", description
        );

        kafkaTemplate.send(
                CRISIS_CREATED_TOPIC,
                crisisId.toString(),
                event
        );

        log.info(
                "Published crisis.created event for crisisId: {}",
                crisisId
        );
    }

    public void publishCrisisResolvedEvent(UUID crisisId) {

        Map<String, Object> event = Map.of(
                "crisisId",
                crisisId.toString()
        );

        kafkaTemplate.send(
                CRISIS_RESOLVED_TOPIC,
                crisisId.toString(),
                event
        );

        log.info(
                "Published crisis.resolved event for crisisId: {}",
                crisisId
        );
    }
}
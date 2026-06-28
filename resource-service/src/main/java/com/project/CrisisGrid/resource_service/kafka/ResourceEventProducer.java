package com.project.CrisisGrid.resource_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String RESOURCE_ALLOCATED_TOPIC = "resource.allocated";

    private static final String RESOURCE_RELEASED_TOPIC = "resource.released";

    public void publishResourceAllocatedEvent(
            UUID crisisId,
            List<UUID> resourceIds) {

        Map<String, Object> event = Map.of(
                "crisisId", crisisId.toString(),
                "resourceIds",
                resourceIds.stream()
                        .map(UUID::toString)
                        .toList()
        );

        kafkaTemplate.send(
                RESOURCE_ALLOCATED_TOPIC,
                crisisId.toString(),
                event
        );

        log.info(
                "Published resource.allocated event for crisis: {}",
                crisisId
        );
    }

    public void publishResourceReleasedEvent(UUID resourceId) {

        Map<String, Object> event = Map.of(
                "resourceId",
                resourceId.toString()
        );

        kafkaTemplate.send(
                RESOURCE_RELEASED_TOPIC,
                resourceId.toString(),
                event
        );

        log.info(
                "Published resource.released event for resource: {}",
                resourceId
        );
    }
}
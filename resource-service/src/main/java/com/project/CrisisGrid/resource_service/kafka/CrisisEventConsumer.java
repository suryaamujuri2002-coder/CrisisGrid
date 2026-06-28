package com.project.CrisisGrid.resource_service.kafka;

import com.project.CrisisGrid.resource_service.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrisisEventConsumer {

    private final ResourceService resourceService;

    @KafkaListener(
            topics = "crisis.resolved",
            groupId = "resource-service-group"
    )
    public void handleCrisisResolvedEvent(
            Map<String, Object> event) {

        try {

            String crisisIdStr = (String) event.get("crisisId");

            UUID crisisId = UUID.fromString(crisisIdStr);

            log.info(
                    "Received crisis.resolved event for crisis: {}",
                    crisisId
            );

            // Auto-release all resources assigned to this crisis
            resourceService.releaseAllResourcesForCrisis(crisisId);

            log.info(
                    "Auto-released all resources for resolved crisis: {}",
                    crisisId
            );

        } catch (Exception e) {

            log.error(
                    "Error processing crisis.resolved event",
                    e
            );
        }
    }
}

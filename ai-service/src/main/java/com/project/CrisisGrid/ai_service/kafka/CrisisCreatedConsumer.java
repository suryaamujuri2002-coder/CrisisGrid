package com.project.CrisisGrid.ai_service.kafka;

import com.project.CrisisGrid.ai_service.agents.AIOrchestrator;
import com.project.CrisisGrid.ai_service.dto.AIAnalysisRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrisisCreatedConsumer {

    private final AIOrchestrator aiOrchestrator;

    @KafkaListener(
            topics = "crisis.created",
            groupId = "ai-service-group"
    )
    public void handleCrisisCreatedEvent(
            Map<String, Object> event) {

        try {

            String crisisIdStr =
                    (String) event.get("crisisId");

            String title =
                    (String) event.get("title");

            String description =
                    (String) event.get("description");

            Double latitude =
                    (Double) event.get("latitude");

            Double longitude =
                    (Double) event.get("longitude");

            UUID crisisId =
                    UUID.fromString(crisisIdStr);

            log.info(
                    "Received crisis.created event for crisis: {}",
                    crisisId
            );

            AIAnalysisRequest request =
                    new AIAnalysisRequest();

            request.setCrisisId(crisisId);
            request.setTitle(title);
            request.setDescription(description);
            request.setLatitude(latitude);
            request.setLongitude(longitude);

            // Trigger AI analysis
            aiOrchestrator.analyzeCrisis(request);

            log.info(
                    "Completed AI analysis for crisis: {}",
                    crisisId
            );

        } catch (Exception e) {

            log.error(
                    "Error processing crisis.created event",
                    e
            );
        }
    }
}
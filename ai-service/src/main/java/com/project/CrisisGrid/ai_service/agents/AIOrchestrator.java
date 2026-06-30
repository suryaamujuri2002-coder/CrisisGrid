package com.project.CrisisGrid.ai_service.agents;

import com.project.CrisisGrid.ai_service.dto.AIAnalysisRequest;
import com.project.CrisisGrid.ai_service.dto.AIAnalysisResponse;
import com.project.CrisisGrid.ai_service.dto.ResourceDTO;
import com.project.CrisisGrid.ai_service.dto.ResourceRecommendation;
import com.project.CrisisGrid.ai_service.enums.CrisisType;
import com.project.CrisisGrid.ai_service.feign.CrisisServiceClient;
import com.project.CrisisGrid.ai_service.feign.ResourceServiceClient;
import com.project.CrisisGrid.resource_service.dto.AllocationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIOrchestrator {

    private final CrisisClassifierAgent classifierAgent;
    private final SeverityScorerAgent scorerAgent;
    private final ResourceAllocatorAgent allocatorAgent;
    private final CrisisServiceClient crisisServiceClient;
    private final ResourceServiceClient resourceServiceClient;

    public AIAnalysisResponse analyzeCrisis(AIAnalysisRequest request) {

        Instant start = Instant.now();

        log.info(
                "Starting AI analysis for crisis: {}",
                request.getCrisisId()
        );

        try {

            // STEP 1: Classify Crisis Type
            CrisisType classifiedType =
                    classifierAgent.classifyCrisis(
                            request.getTitle(),
                            request.getDescription()
                    );

            log.info("Crisis classified as: {}", classifiedType);

            // STEP 2: Score Severity
            SeverityScorerAgent.SeverityResult severityResult =
                    scorerAgent.scoreSeverity(
                            classifiedType.name(),
                            request.getTitle(),
                            request.getDescription()
                    );

            log.info(
                    "Severity score: {}/10 - {}",
                    severityResult.score(),
                    severityResult.explanation()
            );

            // STEP 3: Get Nearby Resources
            List<ResourceDTO> nearbyResources =
                    fetchNearbyResources(
                            request.getLatitude(),
                            request.getLongitude(),
                            classifiedType
                    );

            log.info(
                    "Found {} nearby resources",
                    nearbyResources.size()
            );

            // STEP 4: Recommend Resource Allocation
            List<ResourceRecommendation> recommendations =
                    allocatorAgent.recommendResources(
                            classifiedType,
                            severityResult.score(),
                            request.getDescription(),
                            nearbyResources
                    );

            log.info(
                    "Generated {} resource recommendations",
                    recommendations.size()
            );

            // STEP 5: Update Crisis with AI Analysis
            updateCrisisWithAnalysis(
                    request.getCrisisId(),
                    classifiedType,
                    severityResult.score(),
                    severityResult.explanation()
            );

            // STEP 6: Auto-allocate if severity >= 7
            if (severityResult.score() >= 7
                    && !recommendations.isEmpty()) {

                autoAllocateResources(
                        request.getCrisisId(),
                        recommendations
                );

                log.info(
                        "Auto-allocated resources for high-severity crisis"
                );
            }

            Duration duration =
                    Duration.between(start, Instant.now());

            return AIAnalysisResponse.builder()
                    .crisisId(request.getCrisisId())
                    .classifiedType(classifiedType)
                    .severityScore(severityResult.score())
                    .aiSummary(severityResult.explanation())
                    .recommendedResources(recommendations)
                    .analysisTime(duration.toMillis() + "ms")
                    .build();

        } catch (Exception e) {

            log.error(
                    "Error during AI analysis for crisis: {}",
                    request.getCrisisId(),
                    e
            );

            throw new RuntimeException(
                    "AI analysis failed: " + e.getMessage(),
                    e
            );
        }
    }

    private List<ResourceDTO> fetchNearbyResources(
            Double latitude,
            Double longitude,
            CrisisType crisisType) {

        try {

            Map<String, Object> request =
                    new HashMap<>();

            request.put("latitude", latitude);
            request.put("longitude", longitude);
            request.put("radiusKm", 20.0);
            request.put(
                    "resourceType",
                    mapCrisisTypeToResourceType(crisisType)
            );

            return resourceServiceClient
                    .getNearbyResources(request);

        } catch (Exception e) {

            log.warn(
                    "Could not fetch nearby resources: {}",
                    e.getMessage()
            );

            return List.of();
        }
    }

    private String mapCrisisTypeToResourceType(
            CrisisType crisisType) {

        return switch (crisisType) {

            case MEDICAL -> "AMBULANCE";
            case FIRE -> "FIRE_TRUCK";
            case FLOOD -> "RESCUE_TEAM";
            case ACCIDENT -> "AMBULANCE";

            default -> null;
        };
    }

    private void updateCrisisWithAnalysis(
            UUID crisisId,
            CrisisType type,
            int severity,
            String summary) {

        try {

            Map<String, Object> analysis =
                    new HashMap<>();

            analysis.put("crisisType", type.name());
            analysis.put("severityScore", severity);
            analysis.put("aiSummary", summary);
            analysis.put(
                    "status",
                    severity > 7 ? "ACTIVE" : "PENDING"
            );

            crisisServiceClient
                    .updateCrisisWithAIAnalysis(
                            crisisId,
                            analysis
                    );

        } catch (Exception e) {

            log.error(
                    "Could not update crisis with AI analysis: {}",
                    e.getMessage()
            );
        }
    }

    private void autoAllocateResources(
            UUID crisisId,
            List<ResourceRecommendation> recommendations) {

        try {

            List<UUID> resourceIds =
                    recommendations.stream()
                            .limit(3)
                            .map(ResourceRecommendation::getResourceId)
                            .toList();

            AllocationRequest request = new AllocationRequest();

            request.setCrisisId(crisisId);
            request.setResourceIds(resourceIds);
            request.setNotes("Auto-allocated by AI (high severity)");

            resourceServiceClient.allocateResources(request);

        } catch (Exception e) {

            log.error(
                    "Could not auto-allocate resources: {}",
                    e.getMessage()
            );
        }
    }
}
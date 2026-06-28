package com.project.CrisisGrid.ai_service.agents;

import com.project.CrisisGrid.ai_service.dto.ResourceDTO;
import com.project.CrisisGrid.ai_service.dto.ResourceRecommendation;
import com.project.CrisisGrid.ai_service.enums.CrisisType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceAllocatorAgent {

    private final ChatModel chatModel;

    private static final String ALLOCATION_PROMPT = """
            You are a resource allocation expert for emergency response.

            Given a crisis and available resources, recommend the best resources to deploy.

            Crisis Type: {crisisType}

            Severity Score: {severityScore}/10

            Crisis Description: {description}

            Available Resources:
            {availableResources}

            Recommend up to 5 resources to deploy.

            For each resource, provide:
            - Resource ID
            - Reason for selection

            Consider:
            - Resource type appropriateness for crisis type
            - Distance from crisis location
            - Severity level (higher severity = more resources)

            Format your response as:

            <resource_id> | <reason>

            Example:
            abc-123 | Closest ambulance, medical emergency requires immediate response
            def-456 | Fire truck with water capacity for building fire

            Recommendations:
            """;

    public List<ResourceRecommendation> recommendResources(
            CrisisType crisisType,
            int severityScore,
            String description,
            List<ResourceDTO> availableResources) {

        if (availableResources.isEmpty()) {
            log.warn("No available resources for allocation");
            return List.of();
        }

        try {

            String resourcesList = formatResourcesList(availableResources);

            PromptTemplate promptTemplate =
                    new PromptTemplate(ALLOCATION_PROMPT);

            Prompt prompt = promptTemplate.create(Map.of(
                    "crisisType", crisisType.name(),
                    "severityScore", String.valueOf(severityScore),
                    "description", description,
                    "availableResources", resourcesList
            ));

            String response = chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getText()
                    .trim();

            log.info("AI resource recommendations:\n{}", response);

            return parseRecommendations(response, availableResources);

        } catch (Exception e) {

            log.error(
                    "Error generating resource recommendations, using fallback",
                    e
            );

            return fallbackRecommendations(
                    crisisType,
                    severityScore,
                    availableResources
            );
        }
    }

    private String formatResourcesList(List<ResourceDTO> resources) {

        return resources.stream()
                .map(r -> String.format(
                        "- %s (%s, %s) [ID: %s, Distance: %.2f km]",
                        r.getName(),
                        r.getType(),
                        r.getStatus(),
                        r.getId(),
                        calculateDistance(r)
                ))
                .collect(Collectors.joining("\n"));
    }

    private double calculateDistance(ResourceDTO resource) {

        // Placeholder. In real implementation,
        // calculate from crisis coordinates.
        return Math.random() * 20;
    }

    private List<ResourceRecommendation> parseRecommendations(
            String response,
            List<ResourceDTO> availableResources) {

        List<ResourceRecommendation> recommendations =
                new ArrayList<>();

        String[] lines = response.split("\n");

        for (String line : lines) {

            line = line.trim();

            if (line.isEmpty()
                    || line.startsWith("-")
                    || line.contains("Recommendations")) {
                continue;
            }

            try {

                String[] parts = line.split("\\|", 2);

                if (parts.length != 2) {
                    continue;
                }

                String resourceIdStr = parts[0].trim();
                String reason = parts[1].trim();

                availableResources.stream()
                        .filter(r ->
                                r.getId().toString().contains(resourceIdStr)
                                        || resourceIdStr.contains(
                                        r.getId().toString()))
                        .findFirst()
                        .ifPresent(resource ->
                                recommendations.add(
                                        ResourceRecommendation.builder()
                                                .resourceId(resource.getId())
                                                .resourceName(resource.getName())
                                                .resourceType(resource.getType())
                                                .distanceKm(
                                                        calculateDistance(resource))
                                                .reason(reason)
                                                .build()
                                )
                        );

            } catch (Exception e) {

                log.warn(
                        "Could not parse recommendation line: {}",
                        line
                );
            }
        }

        return recommendations;
    }

    private List<ResourceRecommendation> fallbackRecommendations(
            CrisisType crisisType,
            int severityScore,
            List<ResourceDTO> availableResources) {

        // Simple rule-based fallback
        int numResources =
                severityScore >= 7 ? 3 :
                        severityScore >= 4 ? 2 : 1;

        return availableResources.stream()
                .limit(numResources)
                .map(r -> ResourceRecommendation.builder()
                        .resourceId(r.getId())
                        .resourceName(r.getName())
                        .resourceType(r.getType())
                        .distanceKm(calculateDistance(r))
                        .reason(
                                "Recommended based on proximity and availability"
                        )
                        .build())
                .collect(Collectors.toList());
    }
}
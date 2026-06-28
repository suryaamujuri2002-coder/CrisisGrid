package com.project.CrisisGrid.ai_service.dto;



import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ResourceRecommendation {

    private UUID resourceId;

    private String resourceName;

    private String resourceType;

    private Double distanceKm;

    private String reason;

}
package com.project.CrisisGrid.ai_service.dto;




import com.project.CrisisGrid.ai_service.enums.CrisisType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AIAnalysisResponse {

    private UUID crisisId;

    private CrisisType classifiedType;

    private Integer severityScore;

    private String aiSummary;

    private List<ResourceRecommendation> recommendedResources;

    private String analysisTime;

}

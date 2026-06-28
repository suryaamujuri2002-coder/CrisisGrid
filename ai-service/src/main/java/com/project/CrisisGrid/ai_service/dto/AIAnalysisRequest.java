package com.project.CrisisGrid.ai_service.dto;



import lombok.Data;
import java.util.UUID;

@Data
public class AIAnalysisRequest {

    private UUID crisisId;

    private String title;

    private String description;

    private Double latitude;

    private Double longitude;

}

package com.project.CrisisGrid.ai_service.dto;




import com.project.CrisisGrid.ai_service.enums.CrisisStatus;
import com.project.CrisisGrid.ai_service.enums.CrisisType;
import lombok.Data;

import java.util.UUID;

@Data
public class CrisisDTO {

    private UUID id;

    private String title;

    private String description;

    private Double latitude;

    private Double longitude;

    private String address;

    private CrisisStatus status;

    private CrisisType crisisType;

    private Integer severityScore;

    private String aiSummary;

}

package com.project.CrisisGrid.crisis_service.dto;



import com.project.CrisisGrid.crisis_service.enums.CrisisStatus;
import com.project.CrisisGrid.crisis_service.enums.CrisisType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CrisisReportResponse {

    private UUID id;

    private String title;

    private String description;

    private Double latitude;

    private Double longitude;

    private String address;

    private UUID reportedBy;

    private CrisisStatus status;

    private CrisisType crisisType;

    private Integer severityScore;

    private String aiSummary;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
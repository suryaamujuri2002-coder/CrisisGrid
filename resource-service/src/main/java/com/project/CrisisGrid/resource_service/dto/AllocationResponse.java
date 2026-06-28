package com.project.CrisisGrid.resource_service.dto;

import com.project.CrisisGrid.resource_service.enums.AllocationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AllocationResponse {

    private UUID id;

    private UUID crisisId;

    private UUID resourceId;

    private String resourceName;

    private LocalDateTime allocatedAt;

    private LocalDateTime releasedAt;

    private AllocationType allocatedBy;

    private String notes;
}
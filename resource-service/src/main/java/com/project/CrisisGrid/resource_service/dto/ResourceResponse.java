package com.project.CrisisGrid.resource_service.dto;

import com.project.CrisisGrid.resource_service.enums.ResourceStatus;
import com.project.CrisisGrid.resource_service.enums.ResourceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ResourceResponse {

    private UUID id;

    private String name;

    private ResourceType type;

    private ResourceStatus status;

    private Double currentLatitude;

    private Double currentLongitude;

    private String baseLocation;

    private Integer capacity;

    private UUID assignedCrisisId;

    private String contactNumber;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
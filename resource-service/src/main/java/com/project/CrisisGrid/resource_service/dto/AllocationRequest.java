package com.project.CrisisGrid.resource_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AllocationRequest {

    @NotNull(message = "Crisis ID is required")
    private UUID crisisId;

    @NotEmpty(message = "At least one resource ID is required")
    private List<UUID> resourceIds;

    private String notes;
}
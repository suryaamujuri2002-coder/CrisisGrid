package com.project.CrisisGrid.resource_service.dto;

import com.project.CrisisGrid.resource_service.enums.ResourceType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NearbyResourceRequest {

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private Double longitude;

    private ResourceType resourceType;

    @Min(value = 1, message = "Radius must be at least 1 km")
    @Max(value = 500, message = "Radius cannot exceed 500 km")
    private Double radiusKm = 20.0;
}
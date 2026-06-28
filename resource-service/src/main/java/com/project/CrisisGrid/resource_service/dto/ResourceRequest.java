package com.project.CrisisGrid.resource_service.dto;

import com.project.CrisisGrid.resource_service.enums.ResourceStatus;
import com.project.CrisisGrid.resource_service.enums.ResourceType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResourceRequest {

    @NotBlank(message = "Resource name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Resource type is required")
    private ResourceType type;

    @NotNull(message = "Status is required")
    private ResourceStatus status;

    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private Double currentLatitude;

    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private Double currentLongitude;

    private String baseLocation;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Pattern(
            regexp = "^[+]?[0-9]{10,15}$",
            message = "Invalid contact number"
    )
    private String contactNumber;

    private String description;
}
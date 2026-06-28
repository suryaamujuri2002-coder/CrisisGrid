package com.project.CrisisGrid.crisis_service.dto;



import com.project.CrisisGrid.crisis_service.enums.CrisisType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CrisisReportRequest {

    @NotBlank(message = "Title is required")
    @Size(
            min = 5,
            max = 200,
            message = "Title must be between 5 and 200 characters"
    )
    private String title;

    @NotBlank(message = "Description is required")
    @Size(
            min = 10,
            max = 2000,
            message = "Description must be between 10 and 2000 characters"
    )
    private String description;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private Double longitude;

    private String address;

    private CrisisType crisisType;
}

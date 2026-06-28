package com.project.CrisisGrid.ai_service.dto;



import lombok.Data;

import java.util.UUID;

@Data
public class ResourceDTO {

    private UUID id;

    private String name;

    private String type;

    private String status;

    private Double currentLatitude;

    private Double currentLongitude;

    private Integer capacity;

}
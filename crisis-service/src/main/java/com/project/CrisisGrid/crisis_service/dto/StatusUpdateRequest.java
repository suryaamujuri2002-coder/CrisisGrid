package com.project.CrisisGrid.crisis_service.dto;

import com.project.CrisisGrid.crisis_service.enums.CrisisStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private CrisisStatus status;
}

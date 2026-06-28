package com.project.CrisisGrid.crisis_service.exception;



import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // hides fieldErrors when null
public class ErrorResponse {

    private int status;

    private String error;

    private String message;

    // Populated only for validation errors
    private Map<String, String> fieldErrors;

    private LocalDateTime timestamp;
}

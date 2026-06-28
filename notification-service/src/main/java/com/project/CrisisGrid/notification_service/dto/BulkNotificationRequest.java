package com.project.CrisisGrid.notification_service.dto;

import com.project.CrisisGrid.notification_service.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkNotificationRequest {

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    @NotEmpty(message = "At least one recipient is required")
    private List<String> recipients;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;
}
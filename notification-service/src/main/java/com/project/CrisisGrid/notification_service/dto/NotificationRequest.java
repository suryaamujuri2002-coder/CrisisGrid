package com.project.CrisisGrid.notification_service.dto;

import com.project.CrisisGrid.notification_service.enums.NotificationChannel;
import com.project.CrisisGrid.notification_service.enums.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class NotificationRequest {

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    private NotificationPriority priority = NotificationPriority.NORMAL;

    private Map<String, String> metadata;
}
package com.project.CrisisGrid.notification_service.dto;

import com.project.CrisisGrid.notification_service.enums.NotificationChannel;
import com.project.CrisisGrid.notification_service.enums.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {

    private UUID id;

    private NotificationChannel channel;

    private String recipient;

    private String subject;

    private NotificationStatus status;

    private String errorMessage;

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;
}
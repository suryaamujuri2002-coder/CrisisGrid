package com.project.CrisisGrid.notification_service.service;

import com.project.CrisisGrid.notification_service.dto.NotificationRequest;
import com.project.CrisisGrid.notification_service.dto.NotificationResponse;
import com.project.CrisisGrid.notification_service.enums.NotificationChannel;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse sendNotification(NotificationRequest request);

    List<NotificationResponse> sendBulkNotification(
            NotificationChannel channel,
            List<String> recipients,
            String subject,
            String message
    );

    NotificationResponse getNotificationById(UUID id);

    List<NotificationResponse> getNotificationsByRecipient(String recipient);

    List<NotificationResponse> getNotificationsByCrisis(UUID crisisId);

    void retryFailedNotifications();
}
package com.project.CrisisGrid.notification_service.kafka;

import com.project.CrisisGrid.notification_service.dto.NotificationRequest;
import com.project.CrisisGrid.notification_service.enums.NotificationChannel;
import com.project.CrisisGrid.notification_service.enums.NotificationPriority;
import com.project.CrisisGrid.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrisisEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "crisis.created", groupId = "notification-service-group")
    public void handleCrisisCreatedEvent(Map<String, Object> event) {

        try {

            String crisisId = (String) event.get("crisisId");
            String title = (String) event.get("title");

            log.info("Received crisis.created event for crisis: {}", crisisId);

            // Notify admin/responders about new crisis
            NotificationRequest emailNotification = new NotificationRequest();

            emailNotification.setChannel(NotificationChannel.EMAIL);
            emailNotification.setRecipient("suryaamujuri51@gmail.com"); // In production, fetch from user service
            emailNotification.setSubject("New Crisis Reported: " + title);
            emailNotification.setMessage(String.format(
                    "A new crisis has been reported.\n\nTitle: %s\n\nPlease review and take appropriate action.",
                    title
            ));
            emailNotification.setPriority(NotificationPriority.HIGH);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("crisisId", crisisId);

            emailNotification.setMetadata(metadata);

            notificationService.sendNotification(emailNotification);

        } catch (Exception e) {
            log.error("Error processing crisis.created event", e);
        }
    }

    @KafkaListener(topics = "crisis.resolved", groupId = "notification-service-group")
    public void handleCrisisResolvedEvent(Map<String, Object> event) {

        try {

            String crisisId = (String) event.get("crisisId");

            log.info("Received crisis.resolved event for crisis: {}", crisisId);

            // Notify stakeholders that crisis is resolved
            NotificationRequest notification = new NotificationRequest();

            notification.setChannel(NotificationChannel.EMAIL);
            notification.setRecipient("suryaamujuri51@gmail.com");
            notification.setSubject("Crisis Resolved");
            notification.setMessage(String.format(
                    "Crisis %s has been marked as resolved.\n\nAll allocated resources have been released.",
                    crisisId
            ));
            notification.setPriority(NotificationPriority.NORMAL);

            notificationService.sendNotification(notification);

        } catch (Exception e) {
            log.error("Error processing crisis.resolved event", e);
        }
    }
}
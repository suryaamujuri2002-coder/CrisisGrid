package com.project.CrisisGrid.notification_service.kafka;

import com.project.CrisisGrid.notification_service.dto.NotificationRequest;
import com.project.CrisisGrid.notification_service.enums.NotificationChannel;
import com.project.CrisisGrid.notification_service.enums.NotificationPriority;
import com.project.CrisisGrid.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "resource.allocated", groupId = "notification-service-group")
    public void handleResourceAllocatedEvent(Map<String, Object> event) {

        try {

            String crisisId = (String) event.get("crisisId");

            @SuppressWarnings("unchecked")
            List<String> resourceIds = (List<String>) event.get("resourceIds");

            log.info("Received resource.allocated event for crisis: {}", crisisId);

            // Notify responders about resource allocation
            NotificationRequest smsNotification = new NotificationRequest();

            smsNotification.setChannel(NotificationChannel.SMS);
            smsNotification.setRecipient("+1234567890"); // In production, fetch responder phone from user service
            smsNotification.setSubject("Resource Allocation");

            smsNotification.setMessage(String.format(
                    "ALERT: %d resource(s) have been allocated to crisis %s. Please respond immediately.",
                    resourceIds.size(),
                    crisisId.substring(0, 8)
            ));

            smsNotification.setPriority(NotificationPriority.URGENT);

            notificationService.sendNotification(smsNotification);

        } catch (Exception e) {
            log.error("Error processing resource.allocated event", e);
        }
    }

    @KafkaListener(topics = "resource.released", groupId = "notification-service-group")
    public void handleResourceReleasedEvent(Map<String, Object> event) {

        try {

            String resourceId = (String) event.get("resourceId");

            log.info("Received resource.released event for resource: {}", resourceId);

            // Notify that resource is available again
            NotificationRequest notification = new NotificationRequest();

            notification.setChannel(NotificationChannel.EMAIL);
            notification.setRecipient("dispatch@crisisgrid.com");
            notification.setSubject("Resource Released");

            notification.setMessage(String.format(
                    "Resource %s has been released and is now available for allocation.",
                    resourceId
            ));

            notification.setPriority(NotificationPriority.LOW);

            notificationService.sendNotification(notification);

        } catch (Exception e) {
            log.error("Error processing resource.released event", e);
        }
    }
}
package com.project.CrisisGrid.notification_service.service;



import com.project.CrisisGrid.notification_service.dto.NotificationRequest;
import com.project.CrisisGrid.notification_service.dto.NotificationResponse;
import com.project.CrisisGrid.notification_service.entity.NotificationLog;
import com.project.CrisisGrid.notification_service.enums.NotificationChannel;
import com.project.CrisisGrid.notification_service.enums.NotificationStatus;
import com.project.CrisisGrid.notification_service.exception.NotificationNotFoundException;

import com.project.CrisisGrid.notification_service.repo.NotificationLogRepository;
import com.project.CrisisGrid.notification_service.service.EmailNotificationService;
import com.project.CrisisGrid.notification_service.service.NotificationService;
import com.project.CrisisGrid.notification_service.service.PushNotificationService;
import com.project.CrisisGrid.notification_service.service.SMSNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailNotificationService emailService;
    private final SMSNotificationService smsService;
    private final PushNotificationService pushService;

    @Override
    public NotificationResponse sendNotification(NotificationRequest request) {

        NotificationLog notificationLog = NotificationLog.builder()
                .channel(request.getChannel())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .message(request.getMessage())
                .priority(request.getPriority())
                .status(NotificationStatus.PENDING)
                .build();

        // Extract crisis ID if present
        if (request.getMetadata() != null
                && request.getMetadata().containsKey("crisisId")) {
            notificationLog.setCrisisId(
                    UUID.fromString(request.getMetadata().get("crisisId")));
        }

        NotificationLog saved = notificationLogRepository.save(notificationLog);

        try {

            sendViaChannel(
                    request.getChannel(),
                    request.getRecipient(),
                    request.getSubject(),
                    request.getMessage()
            );

            saved.setStatus(NotificationStatus.SENT);
            saved.setSentAt(LocalDateTime.now());

            log.info("Notification sent successfully via {}: {}",
                    request.getChannel(),
                    saved.getId());

        } catch (Exception e) {

            saved.setStatus(NotificationStatus.FAILED);
            saved.setErrorMessage(e.getMessage());

            log.error("Failed to send notification via {}: {}",
                    request.getChannel(),
                    e.getMessage());
        }

        notificationLogRepository.save(saved);

        return mapToResponse(saved);
    }

    @Override
    public List<NotificationResponse> sendBulkNotification(
            NotificationChannel channel,
            List<String> recipients,
            String subject,
            String message) {

        return recipients.stream()
                .map(recipient -> {

                    NotificationRequest request = new NotificationRequest();
                    request.setChannel(channel);
                    request.setRecipient(recipient);
                    request.setSubject(subject);
                    request.setMessage(message);

                    return sendNotification(request);

                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID id) {

        NotificationLog log = notificationLogRepository.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found with id: " + id));

        return mapToResponse(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByRecipient(String recipient) {

        return notificationLogRepository
                .findByRecipientOrderByCreatedAtDesc(recipient)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByCrisis(UUID crisisId) {

        return notificationLogRepository
                .findByCrisisIdOrderByCreatedAtDesc(crisisId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void retryFailedNotifications() {

        List<NotificationLog> failedNotifications =
                notificationLogRepository.findFailedNotificationsForRetry();

        log.info("Retrying {} failed notifications",
                failedNotifications.size());

        for (NotificationLog notification : failedNotifications) {

            try {

                sendViaChannel(
                        notification.getChannel(),
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getMessage()
                );

                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notification.setErrorMessage(null);

                log.info("Retry successful for notification: {}",
                        notification.getId());

            } catch (Exception e) {

                notification.setRetryCount(
                        notification.getRetryCount() + 1);

                notification.setErrorMessage(e.getMessage());

                log.warn("Retry failed for notification {}: {}",
                        notification.getId(),
                        e.getMessage());
            }

            notificationLogRepository.save(notification);
        }
    }

    // ---------------- Private Helpers ----------------

    private void sendViaChannel(
            NotificationChannel channel,
            String recipient,
            String subject,
            String message) {

        switch (channel) {

            case EMAIL ->
                    emailService.sendEmail(recipient, subject, message);

            case SMS ->
                    smsService.sendSMS(recipient, message);

            case PUSH ->
                    pushService.sendPushNotification(
                            recipient,
                            subject,
                            message);

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported notification channel: " + channel);
        }
    }

    private NotificationResponse mapToResponse(NotificationLog log) {

        return NotificationResponse.builder()
                .id(log.getId())
                .channel(log.getChannel())
                .recipient(log.getRecipient())
                .subject(log.getSubject())
                .status(log.getStatus())
                .errorMessage(log.getErrorMessage())
                .sentAt(log.getSentAt())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
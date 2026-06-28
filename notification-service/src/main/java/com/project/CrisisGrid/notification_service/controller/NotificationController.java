package com.project.CrisisGrid.notification_service.controller;

import com.project.CrisisGrid.notification_service.dto.BulkNotificationRequest;
import com.project.CrisisGrid.notification_service.dto.NotificationRequest;
import com.project.CrisisGrid.notification_service.dto.NotificationResponse;
import com.project.CrisisGrid.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Send a single notification
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONDER')")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {

        log.info("Sending {} notification to {}", request.getChannel(), request.getRecipient());

        NotificationResponse response = notificationService.sendNotification(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Send bulk notifications
     */
    @PostMapping("/send/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> sendBulkNotification(
            @Valid @RequestBody BulkNotificationRequest request) {

        log.info("Sending bulk {} notifications to {} recipients",
                request.getChannel(),
                request.getRecipients().size());

        List<NotificationResponse> responses = notificationService.sendBulkNotification(
                request.getChannel(),
                request.getRecipients(),
                request.getSubject(),
                request.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable UUID id) {

        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    /**
     * Get all notifications for a recipient
     */
    @GetMapping("/recipient/{recipient}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByRecipient(
            @PathVariable String recipient) {

        return ResponseEntity.ok(
                notificationService.getNotificationsByRecipient(recipient)
        );
    }

    /**
     * Get all notifications for a crisis
     */
    @GetMapping("/crisis/{crisisId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByCrisis(
            @PathVariable UUID crisisId) {

        return ResponseEntity.ok(
                notificationService.getNotificationsByCrisis(crisisId)
        );
    }

    /**
     * Retry failed notifications (Admin only)
     */
    @PostMapping("/retry-failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> retryFailedNotifications() {

        log.info("Retrying failed notifications");

        notificationService.retryFailedNotifications();

        return ResponseEntity.noContent().build();
    }
}
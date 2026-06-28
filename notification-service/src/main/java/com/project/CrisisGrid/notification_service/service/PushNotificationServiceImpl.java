package com.project.CrisisGrid.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    @Override
    public void sendPushNotification(String deviceToken, String title, String messageText) {

        try {

            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(messageText)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);

            log.info("Push notification sent successfully: {}", response);

        } catch (Exception e) {

            log.error("Failed to send push notification: {}", e.getMessage());

            throw new RuntimeException("Push notification failed: " + e.getMessage(), e);
        }
    }
}
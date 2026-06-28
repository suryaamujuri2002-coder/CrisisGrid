package com.project.CrisisGrid.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Primary
public class SMSNotificationServiceImpl implements SMSNotificationService {

    @Override
    public void sendSMS(String phoneNumber, String messageText) {

        log.info("MOCK SMS -> {} : {}", phoneNumber, messageText);
        // MOCK SMS LOGIC (no external provider)
        log.info("📱 MOCK SMS SENT");
        log.info("To: {}", phoneNumber);
        log.info("Message: {}", messageText);

        // simulate success response
        System.out.println("\n========= MOCK SMS =========");
        System.out.println("To: " + phoneNumber);
        System.out.println("Message: " + messageText);
        System.out.println("============================\n");
    }
}
package com.project.CrisisGrid.notification_service.service;



public interface EmailNotificationService {

    void sendEmail(String to, String subject, String body);
}
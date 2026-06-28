package com.project.CrisisGrid.notification_service.service;



public interface PushNotificationService {

    void sendPushNotification(String deviceToken, String title, String message);
}
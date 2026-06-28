package com.project.CrisisGrid.notification_service.service;



public interface SMSNotificationService {

    void sendSMS(String phoneNumber, String message);
}
package com.project.CrisisGrid.notification_service.repo;

import com.project.CrisisGrid.notification_service.entity.NotificationLog;
import com.project.CrisisGrid.notification_service.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByRecipientOrderByCreatedAtDesc(String recipient);

    List<NotificationLog> findByCrisisIdOrderByCreatedAtDesc(UUID crisisId);

    List<NotificationLog> findByStatusOrderByCreatedAtDesc(NotificationStatus status);

    @Query("SELECT n FROM NotificationLog n WHERE n.status = 'FAILED' AND n.retryCount < 3")
    List<NotificationLog> findFailedNotificationsForRetry();

    Long countByStatusAndCreatedAtAfter(NotificationStatus status, LocalDateTime after);
}
package com.example.hotelproject.notification.repository;

import com.example.hotelproject.notification.entity.Notification;
import com.example.hotelproject.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByNotificationReference(String notificationReference);

    Page<Notification> findByRecipientEmail(String email, Pageable pageable);

    Page<Notification> findByRecipientEmailAndType(String email, NotificationType type, Pageable pageable);

    Page<Notification> findByBookingId(Long bookingId, Pageable pageable);

    List<Notification> findByStatusAndCreatedAtBefore(String status, LocalDateTime dateTime);

    @Query("SELECT n FROM Notification n WHERE n.recipientEmail = :email AND n.status = :status ORDER BY n.createdAt DESC")
    Page<Notification> findByEmailAndStatus(@Param("email") String email, @Param("status") String status, Pageable pageable);

    long countByRecipientEmailAndStatus(String email, String status);

    long countByRecipientEmailAndReadAtIsNull(String email);
}
package com.example.hotelproject.notification.service;

import com.example.hotelproject.notification.dto.EmailRequestDto;
import com.example.hotelproject.notification.dto.NotificationRequestDto;
import com.example.hotelproject.notification.dto.NotificationResponseDto;
import com.example.hotelproject.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    NotificationResponseDto sendNotification(NotificationRequestDto request);

    NotificationResponseDto sendEmail(EmailRequestDto request);

    NotificationResponseDto sendBookingConfirmation(Long bookingId, String recipientEmail, String recipientName);

    NotificationResponseDto sendBookingCancellation(Long bookingId, String recipientEmail, String recipientName);

    NotificationResponseDto sendPaymentSuccess(Long bookingId, String recipientEmail, String recipientName, Double amount);

    NotificationResponseDto sendPaymentFailed(Long bookingId, String recipientEmail, String recipientName, Double amount);

    NotificationResponseDto getNotificationById(Long id);

    NotificationResponseDto getNotificationByReference(String reference);

    Page<NotificationResponseDto> getNotificationsByEmail(String email, Pageable pageable);

    Page<NotificationResponseDto> getNotificationsByEmailAndType(String email, NotificationType type, Pageable pageable);

    Page<NotificationResponseDto> getNotificationsByBooking(Long bookingId, Pageable pageable);

    void markAsRead(Long notificationId);

    void retryFailedNotifications();

    long getUnreadCount(String email);
}
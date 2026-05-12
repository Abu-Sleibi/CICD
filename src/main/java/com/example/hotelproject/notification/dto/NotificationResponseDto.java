package com.example.hotelproject.notification.dto;

import com.example.hotelproject.notification.entity.NotificationType;
import java.time.LocalDateTime;

public class NotificationResponseDto {

    private final Long id;
    private final String notificationReference;
    private final String recipientEmail;
    private final String recipientName;
    private final NotificationType type;
    private final String subject;
    private final String content;
    private final Long bookingId;
    private final String bookingReference;
    private final String status;
    private final LocalDateTime sentAt;
    private final LocalDateTime readAt;
    private final String failureReason;
    private final LocalDateTime createdAt;

    public NotificationResponseDto(Long id, String notificationReference,
                                   String recipientEmail, String recipientName,
                                   NotificationType type, String subject,
                                   String content, Long bookingId,
                                   String bookingReference, String status,
                                   LocalDateTime sentAt, LocalDateTime readAt,
                                   String failureReason, LocalDateTime createdAt) {
        this.id = id;
        this.notificationReference = notificationReference;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.type = type;
        this.subject = subject;
        this.content = content;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.status = status;
        this.sentAt = sentAt;
        this.readAt = readAt;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getNotificationReference() { return notificationReference; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getRecipientName() { return recipientName; }
    public NotificationType getType() { return type; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public Long getBookingId() { return bookingId; }
    public String getBookingReference() { return bookingReference; }
    public String getStatus() { return status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
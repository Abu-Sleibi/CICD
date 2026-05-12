package com.example.hotelproject.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String notificationReference;

    @Column(nullable = false)
    private String recipientEmail;

    private String recipientName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String subject;

    @Column(length = 5000)
    private String content;

    private Long bookingId;

    private String bookingReference;

    @Column(nullable = false)
    private String status;

    private LocalDateTime sentAt;

    private LocalDateTime readAt;

    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    @PrePersist
    protected void onCreate() {
        this.notificationReference = "NOTIF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public void markAsSent() {
        this.status = "SENT";
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = "FAILED";
        this.failureReason = reason;
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNotificationReference() { return notificationReference; }
    public void setNotificationReference(String notificationReference) { this.notificationReference = notificationReference; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", reference='" + notificationReference + '\'' +
                ", email='" + recipientEmail + '\'' +
                ", type=" + type +
                ", status='" + status + '\'' +
                '}';
    }
}
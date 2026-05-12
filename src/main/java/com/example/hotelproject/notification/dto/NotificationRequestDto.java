package com.example.hotelproject.notification.dto;

import com.example.hotelproject.notification.entity.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationRequestDto {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String recipientEmail;

    private String recipientName;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String content;

    private Long bookingId;

    private String bookingReference;

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
}
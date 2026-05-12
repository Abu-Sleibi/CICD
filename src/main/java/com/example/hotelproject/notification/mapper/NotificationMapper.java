package com.example.hotelproject.notification.mapper;

import com.example.hotelproject.notification.dto.NotificationRequestDto;
import com.example.hotelproject.notification.dto.NotificationResponseDto;
import com.example.hotelproject.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setRecipientEmail(dto.getRecipientEmail());
        notification.setRecipientName(dto.getRecipientName());
        notification.setType(dto.getType());
        notification.setSubject(dto.getSubject());
        notification.setContent(dto.getContent());
        notification.setBookingId(dto.getBookingId());
        notification.setBookingReference(dto.getBookingReference());
        return notification;
    }

    public NotificationResponseDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        return new NotificationResponseDto(
                notification.getId(),
                notification.getNotificationReference(),
                notification.getRecipientEmail(),
                notification.getRecipientName(),
                notification.getType(),
                notification.getSubject(),
                notification.getContent(),
                notification.getBookingId(),
                notification.getBookingReference(),
                notification.getStatus(),
                notification.getSentAt(),
                notification.getReadAt(),
                notification.getFailureReason(),
                notification.getCreatedAt()
        );
    }
}
package com.example.hotelproject.notification.controller;

import com.example.hotelproject.notification.dto.EmailRequestDto;
import com.example.hotelproject.notification.dto.NotificationRequestDto;
import com.example.hotelproject.notification.dto.NotificationResponseDto;
import com.example.hotelproject.notification.entity.NotificationType;
import com.example.hotelproject.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "07. Notifications", description = "Send and manage email notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponseDto> sendNotification(
            @Valid @RequestBody NotificationRequestDto request,
            UriComponentsBuilder uriBuilder) {

        NotificationResponseDto response = notificationService.sendNotification(request);

        URI location = uriBuilder
                .path("/api/v1/notifications/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/email")
    public ResponseEntity<NotificationResponseDto> sendEmail(
            @Valid @RequestBody EmailRequestDto request,
            UriComponentsBuilder uriBuilder) {

        NotificationResponseDto response = notificationService.sendEmail(request);

        URI location = uriBuilder
                .path("/api/v1/notifications/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/booking/{bookingId}/confirmation")
    public ResponseEntity<NotificationResponseDto> sendBookingConfirmation(
            @PathVariable Long bookingId,
            @RequestParam String email,
            @RequestParam(required = false) String name) {

        NotificationResponseDto response = notificationService.sendBookingConfirmation(
                bookingId, email, name != null ? name : "Guest");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/booking/{bookingId}/cancellation")
    public ResponseEntity<NotificationResponseDto> sendBookingCancellation(
            @PathVariable Long bookingId,
            @RequestParam String email,
            @RequestParam(required = false) String name) {

        NotificationResponseDto response = notificationService.sendBookingCancellation(
                bookingId, email, name != null ? name : "Guest");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/booking/{bookingId}/payment/success")
    public ResponseEntity<NotificationResponseDto> sendPaymentSuccess(
            @PathVariable Long bookingId,
            @RequestParam String email,
            @RequestParam(required = false) String name,
            @RequestParam Double amount) {

        NotificationResponseDto response = notificationService.sendPaymentSuccess(
                bookingId, email, name != null ? name : "Guest", amount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/booking/{bookingId}/payment/failed")
    public ResponseEntity<NotificationResponseDto> sendPaymentFailed(
            @PathVariable Long bookingId,
            @RequestParam String email,
            @RequestParam(required = false) String name,
            @RequestParam Double amount) {

        NotificationResponseDto response = notificationService.sendPaymentFailed(
                bookingId, email, name != null ? name : "Guest", amount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDto> getNotificationById(@PathVariable Long id) {
        NotificationResponseDto response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<NotificationResponseDto> getNotificationByReference(@PathVariable String reference) {
        NotificationResponseDto response = notificationService.getNotificationByReference(reference);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getNotificationsByEmail(
            @RequestParam String email,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<NotificationResponseDto> notifications = notificationService.getNotificationsByEmail(email, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<NotificationResponseDto>> getNotificationsByEmailAndType(
            @RequestParam String email,
            @PathVariable NotificationType type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<NotificationResponseDto> notifications = notificationService.getNotificationsByEmailAndType(email, type, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Page<NotificationResponseDto>> getNotificationsByBooking(
            @PathVariable Long bookingId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<NotificationResponseDto> notifications = notificationService.getNotificationsByBooking(bookingId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/retry-failed")
    public ResponseEntity<Void> retryFailedNotifications() {
        notificationService.retryFailedNotifications();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam String email) {
        long count = notificationService.getUnreadCount(email);
        return ResponseEntity.ok(count);
    }
}
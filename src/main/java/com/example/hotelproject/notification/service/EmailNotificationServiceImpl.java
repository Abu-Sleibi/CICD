package com.example.hotelproject.notification.service;

import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.booking.exception.BookingNotFoundException;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.notification.dto.EmailRequestDto;
import com.example.hotelproject.notification.dto.NotificationRequestDto;
import com.example.hotelproject.notification.dto.NotificationResponseDto;
import com.example.hotelproject.notification.entity.Notification;
import com.example.hotelproject.notification.entity.NotificationType;
import com.example.hotelproject.notification.exception.NotificationFailedException;
import com.example.hotelproject.notification.mapper.NotificationMapper;
import com.example.hotelproject.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EmailNotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;
    private final NotificationMapper notificationMapper;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailNotificationServiceImpl(NotificationRepository notificationRepository,
                                        BookingRepository bookingRepository,
                                        NotificationMapper notificationMapper,
                                        JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.bookingRepository = bookingRepository;
        this.notificationMapper = notificationMapper;
        this.mailSender = mailSender;
    }

    @Override
    public NotificationResponseDto sendNotification(NotificationRequestDto request) {
        log.info("Creating notification for: {}, type: {}", request.getRecipientEmail(), request.getType());

        Notification notification = notificationMapper.toEntity(request);
        if (notification == null) {
            throw new NotificationFailedException("Failed to create notification entity");
        }

        Notification savedNotification = notificationRepository.save(notification);

        try {
            sendEmailInternal(savedNotification);
            savedNotification.markAsSent();
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
            savedNotification.markAsFailed(e.getMessage());
        }

        Notification updatedNotification = notificationRepository.save(savedNotification);
        return notificationMapper.toDto(updatedNotification);
    }

    @Override
    public NotificationResponseDto sendEmail(EmailRequestDto request) {
        log.info("Sending email to: {}, subject: {}", request.getTo(), request.getSubject());

        Notification notification = new Notification();
        notification.setRecipientEmail(request.getTo());
        notification.setRecipientName(request.getName());
        notification.setType(NotificationType.BOOKING_CONFIRMATION);
        notification.setSubject(request.getSubject());
        notification.setContent(request.getHtmlContent() != null ? request.getHtmlContent() : request.getTextContent());

        Notification savedNotification = notificationRepository.save(notification);

        try {
            sendEmailInternal(request);
            savedNotification.markAsSent();
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            savedNotification.markAsFailed(e.getMessage());
            throw new NotificationFailedException("Failed to send email: " + e.getMessage(), e);
        }

        Notification updatedNotification = notificationRepository.save(savedNotification);
        return notificationMapper.toDto(updatedNotification);
    }

    @Override
    public NotificationResponseDto sendBookingConfirmation(Long bookingId, String recipientEmail, String recipientName) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        String subject = String.format("Booking Confirmation - %s", booking.getBookingReference());
        String content = buildBookingConfirmationContent(booking);

        NotificationRequestDto request = new NotificationRequestDto();
        request.setRecipientEmail(recipientEmail);
        request.setRecipientName(recipientName);
        request.setType(NotificationType.BOOKING_CONFIRMATION);
        request.setSubject(subject);
        request.setContent(content);
        request.setBookingId(bookingId);
        request.setBookingReference(booking.getBookingReference());

        return sendNotification(request);
    }

    @Override
    public NotificationResponseDto sendBookingCancellation(Long bookingId, String recipientEmail, String recipientName) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        String subject = String.format("Booking Cancellation - %s", booking.getBookingReference());
        String content = buildBookingCancellationContent(booking);

        NotificationRequestDto request = new NotificationRequestDto();
        request.setRecipientEmail(recipientEmail);
        request.setRecipientName(recipientName);
        request.setType(NotificationType.BOOKING_CANCELLATION);
        request.setSubject(subject);
        request.setContent(content);
        request.setBookingId(bookingId);
        request.setBookingReference(booking.getBookingReference());

        return sendNotification(request);
    }

    @Override
    public NotificationResponseDto sendPaymentSuccess(Long bookingId, String recipientEmail, String recipientName, Double amount) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        String subject = "Payment Successful";
        String content = buildPaymentSuccessContent(booking, amount);

        NotificationRequestDto request = new NotificationRequestDto();
        request.setRecipientEmail(recipientEmail);
        request.setRecipientName(recipientName);
        request.setType(NotificationType.PAYMENT_SUCCESS);
        request.setSubject(subject);
        request.setContent(content);
        request.setBookingId(bookingId);
        request.setBookingReference(booking.getBookingReference());

        return sendNotification(request);
    }

    @Override
    public NotificationResponseDto sendPaymentFailed(Long bookingId, String recipientEmail, String recipientName, Double amount) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        String subject = "Payment Failed - Action Required";
        String content = buildPaymentFailedContent(booking, amount);

        NotificationRequestDto request = new NotificationRequestDto();
        request.setRecipientEmail(recipientEmail);
        request.setRecipientName(recipientName);
        request.setType(NotificationType.PAYMENT_FAILED);
        request.setSubject(subject);
        request.setContent(content);
        request.setBookingId(bookingId);
        request.setBookingReference(booking.getBookingReference());

        return sendNotification(request);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDto getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationFailedException("Notification not found with id: " + id));
        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDto getNotificationByReference(String reference) {
        Notification notification = notificationRepository.findByNotificationReference(reference)
                .orElseThrow(() -> new NotificationFailedException("Notification not found with reference: " + reference));
        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotificationsByEmail(String email, Pageable pageable) {
        return notificationRepository.findByRecipientEmail(email, pageable)
                .map(notificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotificationsByEmailAndType(String email, NotificationType type, Pageable pageable) {
        return notificationRepository.findByRecipientEmailAndType(email, type, pageable)
                .map(notificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotificationsByBooking(Long bookingId, Pageable pageable) {
        return notificationRepository.findByBookingId(bookingId, pageable)
                .map(notificationMapper::toDto);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationFailedException("Notification not found with id: " + notificationId));
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void retryFailedNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Notification> failedNotifications = notificationRepository
                .findByStatusAndCreatedAtBefore("FAILED", cutoff);

        for (Notification notification : failedNotifications) {
            try {
                sendEmailInternal(notification);
                notification.markAsSent();
                notificationRepository.save(notification);
                log.info("Retry successful for notification: {}", notification.getNotificationReference());
            } catch (Exception e) {
                log.warn("Retry failed for notification: {}, error: {}",
                        notification.getNotificationReference(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        return notificationRepository.countByRecipientEmailAndReadAtIsNull(email);
    }

    @Async
    protected void sendEmailInternal(Notification notification) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(notification.getRecipientEmail());
        message.setSubject(notification.getSubject());
        message.setText(notification.getContent());

        mailSender.send(message);
    }

    protected void sendEmailInternal(EmailRequestDto request) throws MailException, MessagingException {
        if (request.getHtmlContent() != null && !request.getHtmlContent().isEmpty()) {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getHtmlContent(), true);

            mailSender.send(mimeMessage);
        } else {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(request.getTo());
            message.setSubject(request.getSubject());
            message.setText(request.getTextContent());

            mailSender.send(message);
        }
    }

    private String buildBookingConfirmationContent(Booking booking) {
        StringBuilder content = new StringBuilder();
        content.append("Dear ").append(booking.getGuest().getFullName()).append(",\n\n");
        content.append("Your booking has been confirmed!\n\n");
        content.append("Booking Reference: ").append(booking.getBookingReference()).append("\n");
        content.append("Hotel: ").append(booking.getRoomType().getHotel().getName()).append("\n");
        content.append("Room Type: ").append(booking.getRoomType().getName()).append("\n");
        content.append("Check-in: ").append(booking.getCheckInDate()).append("\n");
        content.append("Check-out: ").append(booking.getCheckOutDate()).append("\n");
        content.append("Guests: ").append(booking.getNumberOfGuests()).append("\n");
        content.append("Total Price: $").append(booking.getTotalPrice()).append("\n\n");
        content.append("Thank you for choosing us!\n");
        return content.toString();
    }

    private String buildBookingCancellationContent(Booking booking) {
        StringBuilder content = new StringBuilder();
        content.append("Dear ").append(booking.getGuest().getFullName()).append(",\n\n");
        content.append("Your booking has been cancelled.\n\n");
        content.append("Booking Reference: ").append(booking.getBookingReference()).append("\n");
        content.append("Hotel: ").append(booking.getRoomType().getHotel().getName()).append("\n\n");
        content.append("If you have any questions, please contact us.\n");
        return content.toString();
    }

    private String buildPaymentSuccessContent(Booking booking, Double amount) {
        StringBuilder content = new StringBuilder();
        content.append("Dear ").append(booking.getGuest().getFullName()).append(",\n\n");
        content.append("Payment of $").append(amount).append(" was successful.\n\n");
        content.append("Booking Reference: ").append(booking.getBookingReference()).append("\n");
        content.append("Total Paid: $").append(booking.getPaidAmount()).append("\n");
        content.append("Remaining Balance: $")
                .append(booking.getTotalPrice().subtract(booking.getPaidAmount())).append("\n\n");
        content.append("Thank you for your payment!\n");
        return content.toString();
    }

    private String buildPaymentFailedContent(Booking booking, Double amount) {
        StringBuilder content = new StringBuilder();
        content.append("Dear ").append(booking.getGuest().getFullName()).append(",\n\n");
        content.append("Payment of $").append(amount).append(" failed.\n\n");
        content.append("Booking Reference: ").append(booking.getBookingReference()).append("\n\n");
        content.append("Please try again or use a different payment method.\n");
        content.append("If the problem persists, please contact your bank.\n");
        return content.toString();
    }
}
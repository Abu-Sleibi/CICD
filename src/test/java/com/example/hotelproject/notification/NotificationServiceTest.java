package com.example.hotelproject.notification;

import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.booking.entity.Guest;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.catalog.hotel.Hotel;
import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.notification.dto.EmailRequestDto;
import com.example.hotelproject.notification.dto.NotificationRequestDto;
import com.example.hotelproject.notification.dto.NotificationResponseDto;
import com.example.hotelproject.notification.entity.Notification;
import com.example.hotelproject.notification.entity.NotificationType;
import com.example.hotelproject.notification.exception.NotificationFailedException;
import com.example.hotelproject.notification.mapper.NotificationMapper;
import com.example.hotelproject.notification.repository.NotificationRepository;
import com.example.hotelproject.notification.service.EmailNotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationServiceImpl notificationService;

    private Notification notification;
    private NotificationRequestDto notificationRequest;
    private NotificationResponseDto notificationResponse;
    private EmailRequestDto emailRequest;
    private Booking booking;
    private Guest guest;
    private RoomType roomType;
    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Hotel");

        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe Room");
        roomType.setHotel(hotel);

        guest = new Guest();
        guest.setId(1L);
        guest.setFullName("Test User");
        guest.setEmail("test@example.com");

        booking = new Booking();
        booking.setId(1L);
        booking.setBookingReference("BK12345678");
        booking.setGuest(guest);
        booking.setRoomType(roomType);
        booking.setTotalPrice(new BigDecimal("450.00"));
        booking.setPaidAmount(new BigDecimal("450.00"));
        booking.setCheckInDate(LocalDate.now().plusDays(5));
        booking.setCheckOutDate(LocalDate.now().plusDays(8));
        booking.setNumberOfGuests(2);
        booking.setNumberOfRooms(1);

        notification = new Notification();
        notification.setId(1L);
        notification.setNotificationReference("NOTIF12345678");
        notification.setRecipientEmail("test@example.com");
        notification.setRecipientName("Test User");
        notification.setType(NotificationType.BOOKING_CONFIRMATION);
        notification.setSubject("Booking Confirmation");
        notification.setContent("Your booking is confirmed");
        notification.setBookingId(1L);
        notification.setBookingReference("BK12345678");
        notification.setStatus("PENDING");
        notification.setCreatedAt(LocalDateTime.now());

        notificationRequest = new NotificationRequestDto();
        notificationRequest.setRecipientEmail("test@example.com");
        notificationRequest.setRecipientName("Test User");
        notificationRequest.setType(NotificationType.BOOKING_CONFIRMATION);
        notificationRequest.setSubject("Booking Confirmation");
        notificationRequest.setContent("Your booking is confirmed");
        notificationRequest.setBookingId(1L);
        notificationRequest.setBookingReference("BK12345678");

        notificationResponse = mock(NotificationResponseDto.class);

        emailRequest = new EmailRequestDto();
        emailRequest.setTo("test@example.com");
        emailRequest.setName("Test User");
        emailRequest.setSubject("Test Email");
        emailRequest.setTextContent("This is a test email");
    }

    @Test
    void sendNotification_Success() throws Exception {
        when(notificationMapper.toEntity(any(NotificationRequestDto.class))).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);
        when(notificationResponse.getId()).thenReturn(1L);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        NotificationResponseDto result = notificationService.sendNotification(notificationRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendNotification_HandlesMailException() throws Exception {
        when(notificationMapper.toEntity(any(NotificationRequestDto.class))).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);
        when(notificationResponse.getId()).thenReturn(1L);
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        NotificationResponseDto result = notificationService.sendNotification(notificationRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendEmail_Success() throws Exception {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);
        when(notificationResponse.getId()).thenReturn(1L);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        NotificationResponseDto result = notificationService.sendEmail(emailRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendEmail_ThrowsException_WhenMailFails() throws Exception {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> notificationService.sendEmail(emailRequest))
                .isInstanceOf(NotificationFailedException.class);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void sendBookingConfirmation_Success() throws Exception {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(notificationMapper.toEntity(any(NotificationRequestDto.class))).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);
        when(notificationResponse.getId()).thenReturn(1L);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        NotificationResponseDto result = notificationService.sendBookingConfirmation(1L, "test@example.com", "Test User");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendBookingConfirmation_ThrowsException_WhenBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.sendBookingConfirmation(99L, "test@example.com", "Test User"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void sendBookingCancellation_Success() throws Exception {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(notificationMapper.toEntity(any(NotificationRequestDto.class))).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);
        when(notificationResponse.getId()).thenReturn(1L);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        NotificationResponseDto result = notificationService.sendBookingCancellation(1L, "test@example.com", "Test User");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendPaymentSuccess_Success() throws Exception {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(notificationMapper.toEntity(any(NotificationRequestDto.class))).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);
        when(notificationResponse.getId()).thenReturn(1L);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        NotificationResponseDto result = notificationService.sendPaymentSuccess(1L, "test@example.com", "Test User", 450.00);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendPaymentFailed_Success() throws Exception {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(notificationMapper.toEntity(any(NotificationRequestDto.class))).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);
        when(notificationResponse.getId()).thenReturn(1L);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        NotificationResponseDto result = notificationService.sendPaymentFailed(1L, "test@example.com", "Test User", 450.00);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void getNotificationById_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);
        when(notificationResponse.getId()).thenReturn(1L);

        NotificationResponseDto result = notificationService.getNotificationById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getNotificationById_ThrowsException_WhenNotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById(99L))
                .isInstanceOf(NotificationFailedException.class);
    }

    @Test
    void getNotificationByReference_Success() {
        when(notificationRepository.findByNotificationReference("NOTIF12345678")).thenReturn(Optional.of(notification));
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);
        when(notificationResponse.getNotificationReference()).thenReturn("NOTIF12345678");

        NotificationResponseDto result = notificationService.getNotificationByReference("NOTIF12345678");

        assertThat(result).isNotNull();
        assertThat(result.getNotificationReference()).isEqualTo("NOTIF12345678");
    }

    @Test
    void getNotificationByReference_ThrowsException_WhenNotFound() {
        when(notificationRepository.findByNotificationReference("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationByReference("INVALID"))
                .isInstanceOf(NotificationFailedException.class);
    }

    @Test
    void getNotificationsByEmail_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notificationPage = new PageImpl<>(Arrays.asList(notification));

        when(notificationRepository.findByRecipientEmail("test@example.com", pageable)).thenReturn(notificationPage);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);

        Page<NotificationResponseDto> result = notificationService.getNotificationsByEmail("test@example.com", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getNotificationsByEmailAndType_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notificationPage = new PageImpl<>(Arrays.asList(notification));

        when(notificationRepository.findByRecipientEmailAndType("test@example.com", NotificationType.BOOKING_CONFIRMATION, pageable))
                .thenReturn(notificationPage);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);

        Page<NotificationResponseDto> result = notificationService.getNotificationsByEmailAndType(
                "test@example.com", NotificationType.BOOKING_CONFIRMATION, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getNotificationsByBooking_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notificationPage = new PageImpl<>(Arrays.asList(notification));

        when(notificationRepository.findByBookingId(1L, pageable)).thenReturn(notificationPage);
        when(notificationMapper.toDto(any(Notification.class))).thenReturn(notificationResponse);

        Page<NotificationResponseDto> result = notificationService.getNotificationsByBooking(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(1L);

        assertThat(notification.getReadAt()).isNotNull();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_ThrowsException_WhenNotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(99L))
                .isInstanceOf(NotificationFailedException.class);
    }

    @Test
    void retryFailedNotifications_Success() throws Exception {
        notification.setStatus("FAILED");
        notification.setCreatedAt(LocalDateTime.now().minusHours(12));
        when(notificationRepository.findByStatusAndCreatedAtBefore(anyString(), any()))
                .thenReturn(Arrays.asList(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        notificationService.retryFailedNotifications();

        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    void getUnreadCount_Success() {
        when(notificationRepository.countByRecipientEmailAndReadAtIsNull("test@example.com")).thenReturn(5L);

        long result = notificationService.getUnreadCount("test@example.com");

        assertThat(result).isEqualTo(5L);
    }
}
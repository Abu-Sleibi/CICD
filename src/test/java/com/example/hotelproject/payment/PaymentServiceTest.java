package com.example.hotelproject.payment;

import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.booking.entity.Guest;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.booking.service.BookingService;
import com.example.hotelproject.payment.dto.PaymentRequestDto;
import com.example.hotelproject.payment.dto.PaymentResponseDto;
import com.example.hotelproject.payment.dto.PaymentWebhookDto;
import com.example.hotelproject.payment.dto.RefundRequestDto;
import com.example.hotelproject.payment.entity.Payment;
import com.example.hotelproject.payment.entity.PaymentMethod;
import com.example.hotelproject.payment.entity.PaymentStatus;
import com.example.hotelproject.payment.exception.PaymentFailedException;
import com.example.hotelproject.payment.exception.PaymentNotFoundException;
import com.example.hotelproject.payment.mapper.PaymentMapper;
import com.example.hotelproject.payment.repository.PaymentRepository;
import com.example.hotelproject.payment.service.MockPaymentServiceImpl;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private MockPaymentServiceImpl paymentService;

    private Payment payment;
    private Booking booking;
    private Guest guest;
    private PaymentRequestDto paymentRequest;
    private PaymentResponseDto paymentResponse;
    private RefundRequestDto refundRequest;
    private PaymentWebhookDto webhookDto;

    @BeforeEach
    void setUp() {
        guest = new Guest();
        guest.setId(1L);
        guest.setFullName("Test User");
        guest.setEmail("test@example.com");

        booking = new Booking();
        booking.setId(1L);
        booking.setBookingReference("BK12345678");
        booking.setGuest(guest);
        booking.setTotalPrice(new BigDecimal("450.00"));
        booking.setPaidAmount(BigDecimal.ZERO);

        payment = new Payment();
        payment.setId(1L);
        payment.setPaymentReference("PAY12345678");
        payment.setBooking(booking);
        payment.setAmount(new BigDecimal("450.00"));
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentIntentId("pi_test_123456");
        payment.setCreatedAt(LocalDateTime.now());

        paymentRequest = new PaymentRequestDto();
        paymentRequest.setBookingId(1L);
        paymentRequest.setAmount(new BigDecimal("450.00"));
        paymentRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        paymentRequest.setPaymentDetails("VISA ending in 4242");

        paymentResponse = mock(PaymentResponseDto.class);

        refundRequest = new RefundRequestDto();
        refundRequest.setPaymentId(1L);
        refundRequest.setAmount(new BigDecimal("450.00"));
        refundRequest.setReason("Customer request");

        webhookDto = new PaymentWebhookDto();
        webhookDto.setEventType("payment.succeeded");
        webhookDto.setPaymentIntentId("pi_test_123456");
        webhookDto.setTransactionId("txn_test_123456");
    }

    @Test
    void createPaymentIntent_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentMapper.toEntity(paymentRequest, booking)).thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);
        when(paymentResponse.getId()).thenReturn(1L);
        when(paymentResponse.getPaymentReference()).thenReturn("PAY12345678");

        PaymentResponseDto result = paymentService.createPaymentIntent(paymentRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPaymentReference()).isEqualTo("PAY12345678");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPaymentIntent_ThrowsException_WhenBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
        paymentRequest.setBookingId(99L);

        assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void createPaymentIntent_ThrowsException_WhenBookingAlreadyPaid() {
        booking.setPaidAmount(new BigDecimal("450.00"));
        booking.setTotalPrice(new BigDecimal("450.00"));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(paymentRequest))
                .isInstanceOf(com.example.hotelproject.payment.exception.PaymentFailedException.class)
                .hasMessageContaining("exceeds remaining booking amount");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processPayment_Success() throws Exception {
        payment.setStatus(PaymentStatus.PENDING);

        lenient().when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        lenient().when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        lenient().when(paymentRepository.findByPaymentIntentId("pi_test_123456")).thenReturn(Optional.of(payment));
        lenient().when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);
        lenient().when(paymentResponse.getId()).thenReturn(1L);
        lenient().when(paymentResponse.getStatus()).thenReturn(PaymentStatus.COMPLETED);
        lenient().when(bookingService.updatePaymentStatus(anyLong(), any())).thenReturn(null);

        // Use reflection to force success
        java.lang.reflect.Field randomField = MockPaymentServiceImpl.class.getDeclaredField("random");
        randomField.setAccessible(true);
        java.util.Random mockRandom = mock(java.util.Random.class);
        when(mockRandom.nextDouble()).thenReturn(0.1); // Less than SUCCESS_RATE (0.85)
        randomField.set(paymentService, mockRandom);

        PaymentResponseDto result = paymentService.processPayment(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void processPayment_ThrowsException_WhenPaymentNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processPayment(99L))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void processPayment_ThrowsException_WhenPaymentNotInPendingState() {
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.processPayment(1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void confirmPayment_Success() {
        when(paymentRepository.findByPaymentIntentId("pi_test_123456")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);
        when(paymentResponse.getId()).thenReturn(1L);
        when(bookingService.updatePaymentStatus(anyLong(), any())).thenReturn(null);

        PaymentResponseDto result = paymentService.confirmPayment("pi_test_123456", "txn_test_123456");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(bookingService).updatePaymentStatus(anyLong(), any());
    }

    @Test
    void confirmPayment_ThrowsException_WhenPaymentIntentNotFound() {
        when(paymentRepository.findByPaymentIntentId("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirmPayment("invalid", "txn"))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void getPaymentById_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);
        when(paymentResponse.getId()).thenReturn(1L);

        PaymentResponseDto result = paymentService.getPaymentById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getPaymentById_ThrowsException_WhenNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(99L))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void getPaymentByReference_Success() {
        when(paymentRepository.findByPaymentReference("PAY12345678")).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);
        when(paymentResponse.getPaymentReference()).thenReturn("PAY12345678");

        PaymentResponseDto result = paymentService.getPaymentByReference("PAY12345678");

        assertThat(result).isNotNull();
        assertThat(result.getPaymentReference()).isEqualTo("PAY12345678");
    }

    @Test
    void getPaymentByReference_ThrowsException_WhenNotFound() {
        when(paymentRepository.findByPaymentReference("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentByReference("INVALID"))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void getPaymentsByBooking_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> paymentPage = new PageImpl<>(Arrays.asList(payment));

        when(paymentRepository.findByBookingId(1L, pageable)).thenReturn(paymentPage);
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);

        Page<PaymentResponseDto> result = paymentService.getPaymentsByBooking(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void refundPayment_Success_FullRefund() {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(new BigDecimal("450.00"));
        payment.setRefundedAmount(null);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);
        when(paymentResponse.getId()).thenReturn(1L);
        when(bookingService.updatePaymentStatus(anyLong(), any())).thenReturn(null);

        PaymentResponseDto result = paymentService.refundPayment(refundRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(bookingService).updatePaymentStatus(anyLong(), any());
    }

    @Test
    void refundPayment_Success_PartialRefund() {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(new BigDecimal("450.00"));
        payment.setRefundedAmount(null);
        refundRequest.setAmount(new BigDecimal("200.00"));

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);
        when(paymentResponse.getId()).thenReturn(1L);
        when(bookingService.updatePaymentStatus(anyLong(), any())).thenReturn(null);

        PaymentResponseDto result = paymentService.refundPayment(refundRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(bookingService).updatePaymentStatus(anyLong(), any());
    }

    @Test
    void refundPayment_ThrowsException_WhenPaymentNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());
        refundRequest.setPaymentId(99L);

        assertThatThrownBy(() -> paymentService.refundPayment(refundRequest))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void refundPayment_ThrowsException_WhenPaymentNotCompleted() {
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refundPayment(refundRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only completed payments can be refunded");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void refundPayment_ThrowsException_WhenAmountExceedsRefundable() {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(new BigDecimal("450.00"));
        payment.setRefundedAmount(new BigDecimal("400.00"));
        refundRequest.setAmount(new BigDecimal("100.00"));

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refundPayment(refundRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void handleWebhook_PaymentSucceeded_Success() {
        when(paymentRepository.findByPaymentIntentId("pi_test_123456")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);
        when(paymentResponse.getId()).thenReturn(1L);
        when(bookingService.updatePaymentStatus(anyLong(), any())).thenReturn(null);

        PaymentResponseDto result = paymentService.handleWebhook(webhookDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void handleWebhook_PaymentFailed_Success() {
        webhookDto.setEventType("payment.failed");

        when(paymentRepository.findByPaymentIntentId("pi_test_123456")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);
        when(paymentResponse.getId()).thenReturn(1L);

        PaymentResponseDto result = paymentService.handleWebhook(webhookDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void handleWebhook_ThrowsException_WhenUnsupportedEvent() {
        webhookDto.setEventType("unsupported.event");

        assertThatThrownBy(() -> paymentService.handleWebhook(webhookDto))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
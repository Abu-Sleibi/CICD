package com.example.hotelproject.payment.service;

import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.booking.exception.BookingNotFoundException;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.booking.service.BookingService;
import com.example.hotelproject.payment.entity.PaymentStatus;
import com.example.hotelproject.payment.dto.PaymentRequestDto;
import com.example.hotelproject.payment.dto.PaymentResponseDto;
import com.example.hotelproject.payment.dto.PaymentWebhookDto;
import com.example.hotelproject.payment.dto.RefundRequestDto;
import com.example.hotelproject.payment.entity.Payment;
import com.example.hotelproject.payment.exception.PaymentFailedException;
import com.example.hotelproject.payment.exception.PaymentNotFoundException;
import com.example.hotelproject.payment.mapper.PaymentMapper;
import com.example.hotelproject.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class MockPaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentServiceImpl.class);
    private static final double SUCCESS_RATE = 0.85;

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final PaymentMapper paymentMapper;
    private final Random random = new Random();

    public MockPaymentServiceImpl(PaymentRepository paymentRepository,
                                  BookingRepository bookingRepository,
                                  BookingService bookingService,
                                  PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentResponseDto createPaymentIntent(PaymentRequestDto request) {
        log.info("Creating payment intent for booking: {}, amount: {}",
                request.getBookingId(), request.getAmount());

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(request.getBookingId()));

        validatePaymentDetails(request, booking);

        if (booking.getPaidAmount().compareTo(booking.getTotalPrice()) >= 0) {
            throw new IllegalStateException("Booking is already fully paid");
        }

        String paymentIntentId = "pi_" + UUID.randomUUID().toString().replace("-", "");

        Payment payment = paymentMapper.toEntity(request, booking);
        payment.setPaymentIntentId(paymentIntentId);
        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment intent created with ID: {}, reference: {}",
                savedPayment.getPaymentIntentId(), savedPayment.getPaymentReference());

        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public PaymentResponseDto processPayment(Long paymentId) {
        log.info("Processing payment with id: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment cannot be processed in status: " + payment.getStatus());
        }

        validatePaymentForProcessing(payment);

        payment.process();
        paymentRepository.save(payment);

        boolean success = random.nextDouble() < SUCCESS_RATE;

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (success) {
            String transactionId = "txn_" + System.currentTimeMillis() + random.nextInt(1000);

            // تأكيد الدفع فقط (ما يؤكدش الحجز)
            payment.complete(transactionId);
            paymentRepository.save(payment);

            log.info("Payment completed successfully: {}", payment.getPaymentReference());

            return paymentMapper.toDto(payment);
        } else {
            payment.fail();
            paymentRepository.save(payment);
            throw new PaymentFailedException("Payment processing failed. Please try again.");
        }
    }


    @Override
    public PaymentResponseDto confirmPayment(String paymentIntentId, String transactionId) {
        log.info("Confirming payment with intent: {}, transaction: {}", paymentIntentId, transactionId);

        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with intent: " + paymentIntentId));

        payment.complete(transactionId);
        Payment confirmedPayment = paymentRepository.save(payment);

        bookingService.updatePaymentStatus(payment.getBooking().getId(), payment.getAmount());

        log.info("Payment confirmed: {}", confirmedPayment.getPaymentReference());

        return paymentMapper.toDto(confirmedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByReference(String reference) {
        Payment payment = paymentRepository.findByPaymentReference(reference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with reference: " + reference));
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getPaymentsByBooking(Long bookingId, Pageable pageable) {
        return paymentRepository.findByBookingId(bookingId, pageable)
                .map(paymentMapper::toDto);
    }

    @Override
    public PaymentResponseDto refundPayment(RefundRequestDto request) {
        log.info("Processing refund for payment: {}, amount: {}", request.getPaymentId(), request.getAmount());

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException(request.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }

        BigDecimal maxRefundable = payment.getAmount().subtract(
                payment.getRefundedAmount() != null ? payment.getRefundedAmount() : BigDecimal.ZERO);

        if (request.getAmount().compareTo(maxRefundable) > 0) {
            throw new IllegalArgumentException(
                    String.format("Refund amount %.2f exceeds refundable amount %.2f",
                            request.getAmount(), maxRefundable));
        }

        payment.refund(request.getAmount());
        Payment refundedPayment = paymentRepository.save(payment);

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            bookingService.updatePaymentStatus(payment.getBooking().getId(),
                    payment.getAmount().negate());
        } else if (payment.getStatus() == PaymentStatus.PARTIALLY_REFUNDED) {
            bookingService.updatePaymentStatus(payment.getBooking().getId(),
                    request.getAmount().negate());
        }

        log.info("Refund processed: {}, new status: {}",
                refundedPayment.getPaymentReference(), refundedPayment.getStatus());

        return paymentMapper.toDto(refundedPayment);
    }

    @Override
    public PaymentResponseDto handleWebhook(PaymentWebhookDto webhookData) {
        log.info("Handling webhook: {}", webhookData.getEventType());

        if ("payment.succeeded".equals(webhookData.getEventType())) {
            return confirmPayment(webhookData.getPaymentIntentId(), webhookData.getTransactionId());
        } else if ("payment.failed".equals(webhookData.getEventType())) {
            Payment payment = paymentRepository.findByPaymentIntentId(webhookData.getPaymentIntentId())
                    .orElseThrow(() -> new PaymentNotFoundException(
                            "Payment not found with intent: " + webhookData.getPaymentIntentId()));

            payment.fail();
            paymentRepository.save(payment);
            return paymentMapper.toDto(payment);
        }

        throw new UnsupportedOperationException("Unsupported webhook event: " + webhookData.getEventType());
    }

    private void validatePaymentDetails(PaymentRequestDto request, Booking booking) {
        // Check for expired card
        if (request.getPaymentDetails() != null && isCardExpired(request.getPaymentDetails())) {
            throw new PaymentFailedException("Card has expired");
        }

        BigDecimal totalPrice = booking.getTotalPrice();
        BigDecimal currentPaid = booking.getPaidAmount();
        BigDecimal remainingAmount = totalPrice.subtract(currentPaid);
        BigDecimal totalAfterPayment = currentPaid.add(request.getAmount());

        // Check if payment exceeds remaining amount
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new PaymentFailedException(
                    String.format("Payment amount %.2f exceeds remaining booking amount %.2f",
                            request.getAmount(), remainingAmount)
            );
        }

        // CHECK FOR PARTIAL PAYMENTS - MUST PAY FULL REMAINING AMOUNT
        if (request.getAmount().compareTo(remainingAmount) < 0) {
            throw new PaymentFailedException(
                    String.format("Partial payments not allowed. Please pay the full remaining amount: %.2f",
                            remainingAmount)
            );
        }
    }

    private void validatePaymentForProcessing(Payment payment) {
        if (payment.getPaymentDetails() != null && isCardExpired(payment.getPaymentDetails())) {
            payment.fail();
            paymentRepository.save(payment);
            throw new PaymentFailedException("Card has expired");
        }

        BigDecimal totalPrice = payment.getBooking().getTotalPrice();
        BigDecimal currentPaid = payment.getBooking().getPaidAmount();
        BigDecimal remainingAmount = totalPrice.subtract(currentPaid);
        BigDecimal totalAfterPayment = currentPaid.add(payment.getAmount());

        if (totalAfterPayment.compareTo(totalPrice) > 0) {
            payment.fail();
            paymentRepository.save(payment);
            throw new PaymentFailedException(
                    String.format("Payment amount %.2f exceeds remaining booking amount %.2f",
                            payment.getAmount(), remainingAmount)
            );
        }

        boolean success = random.nextDouble() < SUCCESS_RATE;

        if (!success) {
            payment.fail();
            paymentRepository.save(payment);
            throw new PaymentFailedException("Payment processing failed. Please try again.");
        }
    }

    private boolean isCardExpired(String paymentDetails) {
        if (paymentDetails == null || paymentDetails.isEmpty()) {
            return false;
        }

        String lowerCaseDetails = paymentDetails.toLowerCase();

        if (lowerCaseDetails.contains("expired")) {
            return true;
        }

        try {
            if (lowerCaseDetails.matches(".*\\d{2}/\\d{2,4}.*")) {
                String expiryStr = lowerCaseDetails.replaceAll(".*?(\\d{2}/\\d{2,4}).*", "$1");
                DateTimeFormatter formatter = expiryStr.length() == 5 ?
                        DateTimeFormatter.ofPattern("MM/yy") : DateTimeFormatter.ofPattern("MM/yyyy");

                YearMonth expiryDate = YearMonth.parse(expiryStr, formatter);
                YearMonth currentDate = YearMonth.now();

                return expiryDate.isBefore(currentDate);
            }
        } catch (DateTimeParseException e) {
            log.debug("Could not parse expiry date from: {}", paymentDetails);
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getAllPayments(Pageable pageable) {
        log.info("Fetching all payments");
        return paymentRepository.findAll(pageable)
                .map(paymentMapper::toDto);
    }

}
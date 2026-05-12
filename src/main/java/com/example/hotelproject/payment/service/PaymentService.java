package com.example.hotelproject.payment.service;

import com.example.hotelproject.payment.dto.PaymentRequestDto;
import com.example.hotelproject.payment.dto.PaymentResponseDto;
import com.example.hotelproject.payment.dto.RefundRequestDto;
import com.example.hotelproject.payment.dto.PaymentWebhookDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentResponseDto createPaymentIntent(PaymentRequestDto request);

    PaymentResponseDto processPayment(Long paymentId);

    PaymentResponseDto confirmPayment(String paymentIntentId, String transactionId);

    PaymentResponseDto getPaymentById(Long id);

    PaymentResponseDto getPaymentByReference(String reference);

    Page<PaymentResponseDto> getPaymentsByBooking(Long bookingId, Pageable pageable);

    PaymentResponseDto refundPayment(RefundRequestDto request);

    PaymentResponseDto handleWebhook(PaymentWebhookDto webhookData);

    Page<PaymentResponseDto> getAllPayments(Pageable pageable);
}
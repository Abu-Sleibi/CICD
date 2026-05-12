package com.example.hotelproject.payment.controller;

import com.example.hotelproject.payment.dto.PaymentRequestDto;
import com.example.hotelproject.payment.dto.PaymentResponseDto;
import com.example.hotelproject.payment.dto.PaymentWebhookDto;
import com.example.hotelproject.payment.dto.RefundRequestDto;
import com.example.hotelproject.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "06. Payment Processing", description = "Mock payment processing and refunds")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-intent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponseDto> createPaymentIntent(
            @Valid @RequestBody PaymentRequestDto request,
            UriComponentsBuilder uriBuilder) {

        PaymentResponseDto response = paymentService.createPaymentIntent(request);

        URI location = uriBuilder
                .path("/api/v1/payments/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponseDto> processPayment(@PathVariable Long id) {
        PaymentResponseDto response = paymentService.processPayment(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponseDto> confirmPayment(
            @RequestParam String paymentIntentId,
            @RequestParam String transactionId) {

        PaymentResponseDto response = paymentService.confirmPayment(paymentIntentId, transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUserByPayment(#id)")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long id) {
        PaymentResponseDto response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<PaymentResponseDto> getPaymentByReference(@PathVariable String reference) {
        PaymentResponseDto response = paymentService.getPaymentByReference(reference);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUserByBooking(#bookingId)")
    public ResponseEntity<Page<PaymentResponseDto>> getPaymentsByBooking(
            @PathVariable Long bookingId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PaymentResponseDto> payments = paymentService.getPaymentsByBooking(bookingId, pageable);
        return ResponseEntity.ok(payments);
    }

    // FIX: 2 Refund endpoint was completely unprotected; restricted to ADMIN
    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponseDto> refundPayment(@Valid @RequestBody RefundRequestDto request) {
        PaymentResponseDto response = paymentService.refundPayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<PaymentResponseDto> handleWebhook(@RequestBody PaymentWebhookDto webhookData) {
        PaymentResponseDto response = paymentService.handleWebhook(webhookData);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponseDto>> getAllPayments(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PaymentResponseDto> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(payments);
    }
}
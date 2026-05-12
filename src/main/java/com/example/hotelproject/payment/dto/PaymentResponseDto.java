package com.example.hotelproject.payment.dto;

import com.example.hotelproject.payment.entity.PaymentMethod;
import com.example.hotelproject.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponseDto {

    private final Long id;
    private final String paymentReference;
    private final Long bookingId;
    private final String bookingReference;
    private final BigDecimal amount;
    private final PaymentMethod paymentMethod;
    private final PaymentStatus status;
    private final String transactionId;
    private final String paymentIntentId;
    private final String paymentDetails;
    private final LocalDateTime createdAt;
    private final LocalDateTime processedAt;
    private final LocalDateTime refundedAt;
    private final BigDecimal refundedAmount;

    public PaymentResponseDto(Long id, String paymentReference, Long bookingId,
                              String bookingReference, BigDecimal amount,
                              PaymentMethod paymentMethod, PaymentStatus status,
                              String transactionId, String paymentIntentId,
                              String paymentDetails, LocalDateTime createdAt,
                              LocalDateTime processedAt, LocalDateTime refundedAt,
                              BigDecimal refundedAmount) {
        this.id = id;
        this.paymentReference = paymentReference;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionId = transactionId;
        this.paymentIntentId = paymentIntentId;
        this.paymentDetails = paymentDetails;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.refundedAt = refundedAt;
        this.refundedAmount = refundedAmount;
    }

    public Long getId() { return id; }
    public String getPaymentReference() { return paymentReference; }
    public Long getBookingId() { return bookingId; }
    public String getBookingReference() { return bookingReference; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public String getTransactionId() { return transactionId; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public String getPaymentDetails() { return paymentDetails; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public BigDecimal getRefundedAmount() { return refundedAmount; }
}
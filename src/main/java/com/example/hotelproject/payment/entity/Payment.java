package com.example.hotelproject.payment.entity;

import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.payment.entity.PaymentMethod;
import com.example.hotelproject.payment.entity.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    private String transactionId;

    private String paymentIntentId;

    @Column(length = 500)
    private String paymentDetails;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private LocalDateTime refundedAt;

    private BigDecimal refundedAmount;

    public Payment() {}

    @PrePersist
    protected void onCreate() {
        this.paymentReference = generatePaymentReference();
        this.createdAt = LocalDateTime.now();
    }

    private String generatePaymentReference() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    public void process() {
        this.status = PaymentStatus.PROCESSING;
    }

    public void complete(String transactionId) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.processedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public void refund(BigDecimal amount) {
        if (this.status == PaymentStatus.COMPLETED) {
            if (amount.compareTo(this.amount) >= 0) {
                this.status = PaymentStatus.REFUNDED;
                this.refundedAmount = this.amount;
            } else {
                this.status = PaymentStatus.PARTIALLY_REFUNDED;
                this.refundedAmount = amount;
            }
            this.refundedAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    public String getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(String paymentDetails) { this.paymentDetails = paymentDetails; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }

    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment payment)) return false;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", reference='" + paymentReference + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                '}';
    }
}
package com.example.hotelproject.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class RefundRequestDto {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal amount;

    private String reason;

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
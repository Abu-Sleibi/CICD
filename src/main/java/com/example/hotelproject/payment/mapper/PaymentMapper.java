package com.example.hotelproject.payment.mapper;

import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.payment.dto.PaymentRequestDto;
import com.example.hotelproject.payment.dto.PaymentResponseDto;
import com.example.hotelproject.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(PaymentRequestDto dto, Booking booking) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentDetails(dto.getPaymentDetails());
        return payment;
    }

    public PaymentResponseDto toDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getBooking().getId(),
                payment.getBooking().getBookingReference(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getPaymentIntentId(),
                payment.getPaymentDetails(),
                payment.getCreatedAt(),
                payment.getProcessedAt(),
                payment.getRefundedAt(),
                payment.getRefundedAmount()
        );
    }
}
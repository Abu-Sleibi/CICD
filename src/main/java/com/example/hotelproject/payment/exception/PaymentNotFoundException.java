package com.example.hotelproject.payment.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Long id) {
        super("Could not find payment with id: " + id);
    }

    public PaymentNotFoundException(String message) {
        super(message);
    }
}
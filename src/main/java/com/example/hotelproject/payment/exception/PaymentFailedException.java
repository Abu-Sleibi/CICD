package com.example.hotelproject.payment.exception;

public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(String message) {
        super(message);
    }
}
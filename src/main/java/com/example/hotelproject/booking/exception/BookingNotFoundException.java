package com.example.hotelproject.booking.exception;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Long id) {
        super("Could not find booking with id: " + id);
    }

    public BookingNotFoundException(String message) {
        super(message);
    }
}
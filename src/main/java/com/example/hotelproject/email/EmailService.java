package com.example.hotelproject.email;

import com.example.hotelproject.booking.entity.Booking;

public interface EmailService {
    void sendBookingConfirmation(Booking booking);
    void sendBookingCancellation(Booking booking);
}

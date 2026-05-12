package com.example.hotelproject.catalog.hotel;

public class HotelNotFoundException extends RuntimeException {

    public HotelNotFoundException(Long id) {
        super("Could not find hotel " + id);
    }

    public HotelNotFoundException(String message) {
        super(message);
    }
}
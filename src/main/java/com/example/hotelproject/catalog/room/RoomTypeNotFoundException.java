package com.example.hotelproject.catalog.room;

public class RoomTypeNotFoundException extends RuntimeException {

    public RoomTypeNotFoundException(Long id) {
        super("Could not find room type " + id);
    }
}
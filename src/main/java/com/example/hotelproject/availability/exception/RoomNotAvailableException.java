package com.example.hotelproject.availability.exception;

public class RoomNotAvailableException extends RuntimeException {

    public RoomNotAvailableException(String message) {
        super(message);
    }

    public RoomNotAvailableException(Long roomTypeId, String date) {
        super("Room type " + roomTypeId + " is not available on " + date);
    }

    public RoomNotAvailableException(Long roomTypeId, String checkIn, String checkOut) {
        super("Room type " + roomTypeId + " is not available from " + checkIn + " to " + checkOut);
    }
}
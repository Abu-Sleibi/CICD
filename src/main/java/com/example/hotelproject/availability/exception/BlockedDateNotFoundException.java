package com.example.hotelproject.availability.exception;

public class BlockedDateNotFoundException extends RuntimeException {

    public BlockedDateNotFoundException(Long id) {
        super("Could not find blocked date with id: " + id);
    }
}
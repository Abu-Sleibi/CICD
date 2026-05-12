package com.example.hotelproject.review.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long id) {
        super("Review not found with id: " + id);
    }

    public ReviewNotFoundException(String message) {
        super(message);
    }
}

package com.example.hotelproject.availability.exception;

public class PricingRuleNotFoundException extends RuntimeException {

    public PricingRuleNotFoundException(Long id) {
        super("Could not find pricing rule with id: " + id);
    }
}
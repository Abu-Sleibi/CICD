package com.example.hotelproject.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CancelBookingRequestDto {

    @NotBlank(message = "Cancellation reason is required")
    @Size(min = 5, max = 200, message = "Reason must be between 5 and 200 characters")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
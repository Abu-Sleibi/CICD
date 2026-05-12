package com.example.hotelproject.availability.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class AvailabilityRequestDto {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    private Long roomTypeId; // If null, check all room types

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "At least 1 guest is required")
    @Max(value = 10, message = "Maximum 10 guests allowed")
    private Integer guests;

    @Min(value = 1, message = "Number of rooms must be at least 1")
    private Integer roomsRequested = 1;

    // Getters and Setters
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getGuests() { return guests; }
    public void setGuests(Integer guests) { this.guests = guests; }

    public Integer getRoomsRequested() { return roomsRequested; }
    public void setRoomsRequested(Integer roomsRequested) { this.roomsRequested = roomsRequested; }

    // Helper method to get number of nights
    public long getNights() {
        return checkInDate != null && checkOutDate != null ?
                java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate) : 0;
    }
}
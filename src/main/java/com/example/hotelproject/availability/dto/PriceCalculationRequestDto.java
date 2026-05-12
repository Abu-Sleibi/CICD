package com.example.hotelproject.availability.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class PriceCalculationRequestDto {

    @NotNull(message = "Room type ID is required")
    private Long roomTypeId;

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    @Min(value = 1, message = "Number of rooms must be at least 1")
    private Integer numberOfRooms = 1;

    private String promoCode; // Optional promo code

    // Getters and Setters
    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getNumberOfRooms() { return numberOfRooms; }
    public void setNumberOfRooms(Integer numberOfRooms) { this.numberOfRooms = numberOfRooms; }

    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }

    public long getNights() {
        return checkInDate != null && checkOutDate != null ?
                java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate) : 0;
    }
}
package com.example.hotelproject.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class BookingRequestDto {

    @NotNull(message = "Room type ID is required")
    private Long roomTypeId;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "At least 1 guest is required")
    @Max(value = 10, message = "Maximum 10 guests allowed")
    private Integer numberOfGuests;

    @NotNull(message = "Number of rooms is required")
    @Min(value = 1, message = "At least 1 room is required")
    private Integer numberOfRooms = 1;

    @Valid
    @NotNull(message = "Guest information is required")
    private GuestDto guest;

    @Size(max = 500, message = "Special requests cannot exceed 500 characters")
    private String specialRequests;

    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public Integer getNumberOfRooms() { return numberOfRooms; }
    public void setNumberOfRooms(Integer numberOfRooms) { this.numberOfRooms = numberOfRooms; }

    public GuestDto getGuest() { return guest; }
    public void setGuest(GuestDto guest) { this.guest = guest; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public long getNights() {
        return checkInDate != null && checkOutDate != null ?
                java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate) : 0;
    }
}
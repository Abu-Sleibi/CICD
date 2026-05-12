package com.example.hotelproject.availability.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class BlockDateRequestDto {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    private Long roomTypeId; // If null, block all room types

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 200, message = "Reason must be between 5 and 200 characters")
    private String reason;

    @Min(value = 1, message = "Number of rooms to block must be at least 1")
    private Integer roomsToBlock; // If null, block all available rooms

    // Getters and Setters
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getRoomsToBlock() { return roomsToBlock; }
    public void setRoomsToBlock(Integer roomsToBlock) { this.roomsToBlock = roomsToBlock; }

    public boolean isBlockAllRooms() {
        return roomsToBlock == null;
    }
}
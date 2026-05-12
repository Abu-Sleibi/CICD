package com.example.hotelproject.availability.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BlockedDateResponseDto {

    private final Long id;
    private final Long hotelId;
    private final String hotelName;
    private final Long roomTypeId;
    private final String roomTypeName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String reason;
    private final Integer blockedRoomsCount;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BlockedDateResponseDto(Long id, Long hotelId, String hotelName,
                                  Long roomTypeId, String roomTypeName,
                                  LocalDate startDate, LocalDate endDate,
                                  String reason, Integer blockedRoomsCount,
                                  boolean active, LocalDateTime createdAt,
                                  LocalDateTime updatedAt) {
        this.id = id;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.blockedRoomsCount = blockedRoomsCount;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getId() { return id; }
    public Long getHotelId() { return hotelId; }
    public String getHotelName() { return hotelName; }
    public Long getRoomTypeId() { return roomTypeId; }
    public String getRoomTypeName() { return roomTypeName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getReason() { return reason; }
    public Integer getBlockedRoomsCount() { return blockedRoomsCount; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
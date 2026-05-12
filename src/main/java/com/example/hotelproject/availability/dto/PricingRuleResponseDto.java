package com.example.hotelproject.availability.dto;

import com.example.hotelproject.enums.DayType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PricingRuleResponseDto {

    private final Long id;
    private final Long hotelId;
    private final String hotelName;
    private final Long roomTypeId;
    private final String roomTypeName;
    private final String name;
    private final String description;
    private final DayType dayType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BigDecimal multiplier;
    private final BigDecimal fixedAdjustment;
    private final Integer minStay;
    private final Integer priority;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PricingRuleResponseDto(Long id, Long hotelId, String hotelName,
                                  Long roomTypeId, String roomTypeName,
                                  String name, String description, DayType dayType,
                                  LocalDate startDate, LocalDate endDate,
                                  BigDecimal multiplier, BigDecimal fixedAdjustment,
                                  Integer minStay, Integer priority, boolean active,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.name = name;
        this.description = description;
        this.dayType = dayType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.multiplier = multiplier;
        this.fixedAdjustment = fixedAdjustment;
        this.minStay = minStay;
        this.priority = priority;
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
    public String getName() { return name; }
    public String getDescription() { return description; }
    public DayType getDayType() { return dayType; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getMultiplier() { return multiplier; }
    public BigDecimal getFixedAdjustment() { return fixedAdjustment; }
    public Integer getMinStay() { return minStay; }
    public Integer getPriority() { return priority; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
package com.example.hotelproject.availability.dto;

import com.example.hotelproject.enums.DayType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PricingRuleRequestDto {

    private Long hotelId; // If null, applies to all hotels

    private Long roomTypeId; // If null, applies to all room types

    @NotBlank(message = "Rule name is required")
    @Size(min = 3, max = 100, message = "Rule name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private DayType dayType; // If null, applies to all days

    private LocalDate startDate; // For seasonal pricing

    private LocalDate endDate; // For seasonal pricing

    @DecimalMin(value = "0.0", inclusive = false, message = "Multiplier must be greater than 0")
    @Digits(integer = 3, fraction = 2, message = "Invalid multiplier format")
    private BigDecimal multiplier; // e.g., 1.5 for 50% increase

    @Digits(integer = 6, fraction = 2, message = "Invalid adjustment format")
    private BigDecimal fixedAdjustment; // e.g., +20.00 or -10.00

    @Min(value = 1, message = "Minimum stay must be at least 1")
    private Integer minStay;

    @NotNull(message = "Priority is required")
    @Min(value = 0, message = "Priority must be at least 0")
    @Max(value = 100, message = "Priority cannot exceed 100")
    private Integer priority = 0;

    // Getters and Setters
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DayType getDayType() { return dayType; }
    public void setDayType(DayType dayType) { this.dayType = dayType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getMultiplier() { return multiplier; }
    public void setMultiplier(BigDecimal multiplier) { this.multiplier = multiplier; }

    public BigDecimal getFixedAdjustment() { return fixedAdjustment; }
    public void setFixedAdjustment(BigDecimal fixedAdjustment) { this.fixedAdjustment = fixedAdjustment; }

    public Integer getMinStay() { return minStay; }
    public void setMinStay(Integer minStay) { this.minStay = minStay; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
}
package com.example.hotelproject.availability.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PriceCalculationResponseDto {

    private final Long roomTypeId;
    private final String roomTypeName;
    private final Long hotelId;
    private final String hotelName;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final long nights;
    private final Integer numberOfRooms;
    private final BigDecimal basePricePerNight;
    private final BigDecimal finalPricePerNight;
    private final BigDecimal subtotal;
    private final BigDecimal discounts;
    private final BigDecimal taxes;
    private final BigDecimal totalPrice;
    private final List<PriceBreakdown> dailyBreakdown;
    private final List<AppliedRule> appliedRules;

    public PriceCalculationResponseDto(Long roomTypeId, String roomTypeName,
                                       Long hotelId, String hotelName,
                                       LocalDate checkInDate, LocalDate checkOutDate,
                                       long nights, Integer numberOfRooms,
                                       BigDecimal basePricePerNight, BigDecimal finalPricePerNight,
                                       BigDecimal subtotal, BigDecimal discounts,
                                       BigDecimal taxes, BigDecimal totalPrice,
                                       List<PriceBreakdown> dailyBreakdown,
                                       List<AppliedRule> appliedRules) {
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.nights = nights;
        this.numberOfRooms = numberOfRooms;
        this.basePricePerNight = basePricePerNight;
        this.finalPricePerNight = finalPricePerNight;
        this.subtotal = subtotal;
        this.discounts = discounts;
        this.taxes = taxes;
        this.totalPrice = totalPrice;
        this.dailyBreakdown = dailyBreakdown;
        this.appliedRules = appliedRules;
    }

    // Getters
    public Long getRoomTypeId() { return roomTypeId; }
    public String getRoomTypeName() { return roomTypeName; }
    public Long getHotelId() { return hotelId; }
    public String getHotelName() { return hotelName; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public long getNights() { return nights; }
    public Integer getNumberOfRooms() { return numberOfRooms; }
    public BigDecimal getBasePricePerNight() { return basePricePerNight; }
    public BigDecimal getFinalPricePerNight() { return finalPricePerNight; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscounts() { return discounts; }
    public BigDecimal getTaxes() { return taxes; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public List<PriceBreakdown> getDailyBreakdown() { return dailyBreakdown; }
    public List<AppliedRule> getAppliedRules() { return appliedRules; }

    // Inner class for daily price breakdown
    public static class PriceBreakdown {
        private final LocalDate date;
        private final BigDecimal basePrice;
        private final BigDecimal adjustedPrice;
        private final BigDecimal pricePerRoom;
        private final BigDecimal totalForDay;

        public PriceBreakdown(LocalDate date, BigDecimal basePrice,
                              BigDecimal adjustedPrice, BigDecimal pricePerRoom,
                              BigDecimal totalForDay) {
            this.date = date;
            this.basePrice = basePrice;
            this.adjustedPrice = adjustedPrice;
            this.pricePerRoom = pricePerRoom;
            this.totalForDay = totalForDay;
        }

        // Getters
        public LocalDate getDate() { return date; }
        public BigDecimal getBasePrice() { return basePrice; }
        public BigDecimal getAdjustedPrice() { return adjustedPrice; }
        public BigDecimal getPricePerRoom() { return pricePerRoom; }
        public BigDecimal getTotalForDay() { return totalForDay; }
    }

    // Inner class for applied pricing rules
    public static class AppliedRule {
        private final String ruleName;
        private final String description;
        private final BigDecimal adjustment;

        public AppliedRule(String ruleName, String description, BigDecimal adjustment) {
            this.ruleName = ruleName;
            this.description = description;
            this.adjustment = adjustment;
        }

        // Getters
        public String getRuleName() { return ruleName; }
        public String getDescription() { return description; }
        public BigDecimal getAdjustment() { return adjustment; }
    }
}
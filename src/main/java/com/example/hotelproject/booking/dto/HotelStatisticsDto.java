package com.example.hotelproject.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HotelStatisticsDto {
    private final Long hotelId;
    private final String hotelName;
    private final long totalBookings;
    private final long confirmedBookings;
    private final long pendingBookings;
    private final long cancelledBookings;
    private final long completedBookings;
    private final BigDecimal totalRevenue;
    private final BigDecimal averageBookingValue;
    private final long totalGuests;
    private final double occupancyRate;
    private final LocalDateTime calculatedAt;

    public HotelStatisticsDto(Long hotelId, String hotelName, long totalBookings,
                              long confirmedBookings, long pendingBookings,
                              long cancelledBookings, long completedBookings,
                              BigDecimal totalRevenue, BigDecimal averageBookingValue,
                              long totalGuests, double occupancyRate) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.totalBookings = totalBookings;
        this.confirmedBookings = confirmedBookings;
        this.pendingBookings = pendingBookings;
        this.cancelledBookings = cancelledBookings;
        this.completedBookings = completedBookings;
        this.totalRevenue = totalRevenue;
        this.averageBookingValue = averageBookingValue;
        this.totalGuests = totalGuests;
        this.occupancyRate = occupancyRate;
        this.calculatedAt = LocalDateTime.now();
    }

    public Long getHotelId() { return hotelId; }
    public String getHotelName() { return hotelName; }
    public long getTotalBookings() { return totalBookings; }
    public long getConfirmedBookings() { return confirmedBookings; }
    public long getPendingBookings() { return pendingBookings; }
    public long getCancelledBookings() { return cancelledBookings; }
    public long getCompletedBookings() { return completedBookings; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getAverageBookingValue() { return averageBookingValue; }
    public long getTotalGuests() { return totalGuests; }
    public double getOccupancyRate() { return occupancyRate; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
}
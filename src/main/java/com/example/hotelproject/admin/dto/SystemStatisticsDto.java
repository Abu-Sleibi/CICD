package com.example.hotelproject.admin.dto;

import java.math.BigDecimal;

public class SystemStatisticsDto {

    private long totalHotels;
    private long activeHotels;
    private long totalRoomTypes;
    private long totalUsers;
    private long totalGuests;

    private long totalBookings;
    private long pendingBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private long completedBookings;
    private long noShowBookings;

    private BigDecimal totalRevenue;
    private BigDecimal averageBookingValue;

    private long totalPayments;
    private BigDecimal totalRefunded;

    private long totalNotifications;
    private long totalReviews;
    private Double overallAverageRating;

    public SystemStatisticsDto() {}

    public long getTotalHotels() { return totalHotels; }
    public void setTotalHotels(long totalHotels) { this.totalHotels = totalHotels; }

    public long getActiveHotels() { return activeHotels; }
    public void setActiveHotels(long activeHotels) { this.activeHotels = activeHotels; }

    public long getTotalRoomTypes() { return totalRoomTypes; }
    public void setTotalRoomTypes(long totalRoomTypes) { this.totalRoomTypes = totalRoomTypes; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalGuests() { return totalGuests; }
    public void setTotalGuests(long totalGuests) { this.totalGuests = totalGuests; }

    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }

    public long getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(long pendingBookings) { this.pendingBookings = pendingBookings; }

    public long getConfirmedBookings() { return confirmedBookings; }
    public void setConfirmedBookings(long confirmedBookings) { this.confirmedBookings = confirmedBookings; }

    public long getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(long cancelledBookings) { this.cancelledBookings = cancelledBookings; }

    public long getCompletedBookings() { return completedBookings; }
    public void setCompletedBookings(long completedBookings) { this.completedBookings = completedBookings; }

    public long getNoShowBookings() { return noShowBookings; }
    public void setNoShowBookings(long noShowBookings) { this.noShowBookings = noShowBookings; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getAverageBookingValue() { return averageBookingValue; }
    public void setAverageBookingValue(BigDecimal averageBookingValue) { this.averageBookingValue = averageBookingValue; }

    public long getTotalPayments() { return totalPayments; }
    public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }

    public BigDecimal getTotalRefunded() { return totalRefunded; }
    public void setTotalRefunded(BigDecimal totalRefunded) { this.totalRefunded = totalRefunded; }

    public long getTotalNotifications() { return totalNotifications; }
    public void setTotalNotifications(long totalNotifications) { this.totalNotifications = totalNotifications; }

    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }

    public Double getOverallAverageRating() { return overallAverageRating; }
    public void setOverallAverageRating(Double overallAverageRating) { this.overallAverageRating = overallAverageRating; }
}

package com.example.hotelproject.availability.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AvailabilityResponseDto {

    private final Long hotelId;
    private final String hotelName;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final long nights;
    private final Integer guests;
    private final boolean available;
    private final List<RoomAvailabilityDetail> roomDetails;

    public AvailabilityResponseDto(Long hotelId, String hotelName,
                                   LocalDate checkInDate, LocalDate checkOutDate,
                                   long nights, Integer guests, boolean available,
                                   List<RoomAvailabilityDetail> roomDetails) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.nights = nights;
        this.guests = guests;
        this.available = available;
        this.roomDetails = roomDetails;
    }

    public Long getHotelId() { return hotelId; }
    public String getHotelName() { return hotelName; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public long getNights() { return nights; }
    public Integer getGuests() { return guests; }
    public boolean isAvailable() { return available; }
    public List<RoomAvailabilityDetail> getRoomDetails() { return roomDetails; }

    public static class RoomAvailabilityDetail {
        private final Long roomTypeId;
        private final String roomTypeName;
        private final Integer capacity;
        private final Integer availableRooms;
        private final BigDecimal pricePerNight;
        private final BigDecimal totalPrice;
        private final List<DailyAvailability> dailyBreakdown;

        public RoomAvailabilityDetail(Long roomTypeId, String roomTypeName,
                                      Integer capacity, Integer availableRooms,
                                      BigDecimal pricePerNight, BigDecimal totalPrice,
                                      List<DailyAvailability> dailyBreakdown) {
            this.roomTypeId = roomTypeId;
            this.roomTypeName = roomTypeName;
            this.capacity = capacity;
            this.availableRooms = availableRooms;
            this.pricePerNight = pricePerNight;
            this.totalPrice = totalPrice;
            this.dailyBreakdown = dailyBreakdown;
        }

        public Long getRoomTypeId() { return roomTypeId; }
        public String getRoomTypeName() { return roomTypeName; }
        public Integer getCapacity() { return capacity; }
        public Integer getAvailableRooms() { return availableRooms; }
        public BigDecimal getPricePerNight() { return pricePerNight; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public List<DailyAvailability> getDailyBreakdown() { return dailyBreakdown; }
    }

    public static class DailyAvailability {
        private final LocalDate date;
        private final boolean available;
        private final Integer availableRooms;
        private final BigDecimal price;

        public DailyAvailability(LocalDate date, boolean available,
                                 Integer availableRooms, BigDecimal price) {
            this.date = date;
            this.available = available;
            this.availableRooms = availableRooms;
            this.price = price;
        }

        public LocalDate getDate() { return date; }
        public boolean isAvailable() { return available; }
        public Integer getAvailableRooms() { return availableRooms; }
        public BigDecimal getPrice() { return price; }
    }
}
package com.example.hotelproject.catalog.room;

import java.math.BigDecimal;
import java.util.List;

public class RoomTypeResponseDto {

    private final Long id;
    private final String name;
    private final String description;
    private final Integer capacity;
    private final BigDecimal basePrice;
    private final List<String> amenities;
    private final Integer totalRooms;
    private final boolean active;
    private final Long hotelId;
    private final String hotelName;

    public RoomTypeResponseDto(Long id, String name, String description, Integer capacity,
                               BigDecimal basePrice, List<String> amenities, Integer totalRooms,
                               boolean active, Long hotelId, String hotelName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.amenities = amenities;
        this.totalRooms = totalRooms;
        this.active = active;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getCapacity() { return capacity; }
    public BigDecimal getBasePrice() { return basePrice; }
    public BigDecimal getPricePerNight() { return basePrice; }
    public List<String> getAmenities() { return amenities; }
    public Integer getTotalRooms() { return totalRooms; }
    public boolean isActive() { return active; }
    public Long getHotelId() { return hotelId; }
    public String getHotelName() { return hotelName; }
}
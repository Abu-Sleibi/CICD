package com.example.hotelproject.catalog.room;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class RoomTypeRequestDto {

    @NotBlank(message = "Room type name must not be blank")
    @Size(min = 2, max = 50, message = "Room type name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Description must not be blank")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10, message = "Capacity must be at most 10")
    private Integer capacity;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
    @Digits(integer = 6, fraction = 2, message = "Invalid price format")
    private BigDecimal basePrice;

    private List<String> amenities;

    @NotNull(message = "Total rooms is required")
    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public Integer getTotalRooms() { return totalRooms; }
    public void setTotalRooms(Integer totalRooms) { this.totalRooms = totalRooms; }

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
}
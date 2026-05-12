package com.example.hotelproject.catalog.hotel;

import com.example.hotelproject.catalog.room.RoomTypeResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public class HotelResponseDto {

    private final Long id;
    private final String name;
    private final String description;
    private final String address;
    private final String city;
    private final String country;
    private final String postalCode;
    private final String phone;
    private final String email;
    private final Integer starRating;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<RoomTypeResponseDto> roomTypes;
    private final List<String> images;

    public HotelResponseDto(Long id, String name, String description, String address,
                            String city, String country, String postalCode, String phone,
                            String email, Integer starRating, boolean active,
                            LocalDateTime createdAt, LocalDateTime updatedAt,
                            List<RoomTypeResponseDto> roomTypes, List<String> images) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.phone = phone;
        this.email = email;
        this.starRating = starRating;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roomTypes = roomTypes;
        this.images = images;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getPostalCode() { return postalCode; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public Integer getStarRating() { return starRating; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<RoomTypeResponseDto> getRoomTypes() { return roomTypes; }
    public List<String> getImages() { return images; }
}
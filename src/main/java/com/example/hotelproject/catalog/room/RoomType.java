package com.example.hotelproject.catalog.room;

import com.example.hotelproject.catalog.hotel.Hotel;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "room_types")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @ElementCollection
    @CollectionTable(name = "room_type_amenities", joinColumns = @JoinColumn(name = "room_type_id"))
    @Column(name = "amenity")
    private List<String> amenities = new ArrayList<>();

    @Column(nullable = false)
    private Integer totalRooms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @JsonBackReference
    private Hotel hotel;

    @Column(nullable = false)
    private boolean active = true;

    public RoomType() {}

    public RoomType(String name, String description, Integer capacity, BigDecimal basePrice, Integer totalRooms) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.totalRooms = totalRooms;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomType roomType)) return false;
        return Objects.equals(id, roomType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RoomType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", basePrice=" + basePrice +
                '}';
    }
}
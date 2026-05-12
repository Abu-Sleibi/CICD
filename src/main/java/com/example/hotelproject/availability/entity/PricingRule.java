package com.example.hotelproject.availability.entity;

import com.example.hotelproject.catalog.hotel.Hotel;
import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.enums.DayType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "pricing_rules")
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel; // If null, applies to all hotels

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType; // If null, applies to all room types

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type")
    private DayType dayType; // WEEKDAY, WEEKEND, or null for all days

    @Column(name = "start_date")
    private LocalDate startDate; // For seasonal pricing

    @Column(name = "end_date")
    private LocalDate endDate; // For seasonal pricing

    @Column(name = "multiplier", precision = 5, scale = 2)
    private BigDecimal multiplier; // e.g., 1.5 for 50% increase

    @Column(name = "fixed_adjustment", precision = 10, scale = 2)
    private BigDecimal fixedAdjustment; // e.g., +20.00 or -10.00

    @Column(name = "min_stay")
    private Integer minStay; // Minimum nights required

    @Column(name = "priority", nullable = false)
    private Integer priority = 0; // Higher priority overrides lower

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public PricingRule() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business method to check if rule applies to a specific date
    public boolean appliesTo(LocalDate date, DayType dayType) {
        if (!active) return false;

        // Check date range if specified
        if (startDate != null && date.isBefore(startDate)) return false;
        if (endDate != null && date.isAfter(endDate)) return false;

        // Check day type if specified
        if (this.dayType != null && this.dayType != dayType) return false;

        return true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

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

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PricingRule that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PricingRule{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", multiplier=" + multiplier +
                '}';
    }
}
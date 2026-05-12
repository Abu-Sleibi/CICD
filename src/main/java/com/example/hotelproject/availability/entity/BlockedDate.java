package com.example.hotelproject.availability.entity;

import com.example.hotelproject.catalog.hotel.Hotel;
import com.example.hotelproject.catalog.room.RoomType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "blocked_dates")
public class BlockedDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType; // If null, block all room types in hotel

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String reason;

    @Column(name = "blocked_rooms_count")
    private Integer blockedRoomsCount; // How many rooms to block (null = all)

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public BlockedDate() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
    public boolean isDateInRange(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean appliesTo(RoomType roomType) {
        return this.roomType == null || this.roomType.equals(roomType);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getBlockedRoomsCount() { return blockedRoomsCount; }
    public void setBlockedRoomsCount(Integer blockedRoomsCount) { this.blockedRoomsCount = blockedRoomsCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockedDate that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BlockedDate{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", reason='" + reason + '\'' +
                '}';
    }
}
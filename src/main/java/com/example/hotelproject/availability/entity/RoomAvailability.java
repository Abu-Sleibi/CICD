package com.example.hotelproject.availability.entity;

import com.example.hotelproject.catalog.room.RoomType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "room_availability",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_type_id", "date"}))
public class RoomAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer totalRooms;

    @Column(nullable = false)
    private Integer bookedRooms = 0;

    @Column(nullable = false)
    private Integer heldRooms = 0;  // Pending bookings (15-minute hold)

    @Column(nullable = false)
    private Integer blockedRooms = 0; // Maintenance or other blocks

    @Column(precision = 10, scale = 2)
    private BigDecimal customPrice; // If null, use base price + pricing rules

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    public RoomAvailability() {}

    public RoomAvailability(RoomType roomType, LocalDate date, Integer totalRooms) {
        this.roomType = roomType;
        this.date = date;
        this.totalRooms = totalRooms;
        this.lastUpdated = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Business methods
    public Integer getAvailableRooms() {
        return totalRooms - bookedRooms - heldRooms - blockedRooms;
    }

    public boolean isAvailable(int requestedRooms) {
        return getAvailableRooms() >= requestedRooms;
    }

    public void holdRooms(int rooms) {
        if (!isAvailable(rooms)) {
            throw new IllegalStateException("Not enough rooms available to hold");
        }
        this.heldRooms += rooms;
    }

    public void releaseHold(int rooms) {
        if (this.heldRooms < rooms) {
            throw new IllegalStateException("Cannot release more holds than exist");
        }
        this.heldRooms -= rooms;
    }

    public void confirmBooking(int rooms) {
        if (this.heldRooms < rooms) {
            throw new IllegalStateException("Not enough held rooms to confirm");
        }
        this.heldRooms -= rooms;
        this.bookedRooms += rooms;
    }

    public void cancelBooking(int rooms) {
        if (this.bookedRooms < rooms) {
            throw new IllegalStateException("Cannot cancel more bookings than exist");
        }
        this.bookedRooms -= rooms;
    }

    public void blockRooms(int rooms) {
        if (getAvailableRooms() < rooms) {
            throw new IllegalStateException("Not enough rooms available to block");
        }
        this.blockedRooms += rooms;
    }

    public void unblockRooms(int rooms) {
        if (this.blockedRooms < rooms) {
            throw new IllegalStateException("Cannot unblock more rooms than are blocked");
        }
        this.blockedRooms -= rooms;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getTotalRooms() { return totalRooms; }
    public void setTotalRooms(Integer totalRooms) { this.totalRooms = totalRooms; }

    public Integer getBookedRooms() { return bookedRooms; }
    public void setBookedRooms(Integer bookedRooms) { this.bookedRooms = bookedRooms; }

    public Integer getHeldRooms() { return heldRooms; }
    public void setHeldRooms(Integer heldRooms) { this.heldRooms = heldRooms; }

    public Integer getBlockedRooms() { return blockedRooms; }
    public void setBlockedRooms(Integer blockedRooms) { this.blockedRooms = blockedRooms; }

    public BigDecimal getCustomPrice() { return customPrice; }
    public void setCustomPrice(BigDecimal customPrice) { this.customPrice = customPrice; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomAvailability that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RoomAvailability{" +
                "id=" + id +
                ", roomType=" + (roomType != null ? roomType.getName() : null) +
                ", date=" + date +
                ", available=" + getAvailableRooms() +
                '}';
    }
}
package com.example.hotelproject.booking.entity;

import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.enums.BookingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private Integer numberOfRooms;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private Integer numberOfGuests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(length = 500)
    private String specialRequests;

    private LocalDateTime confirmedAt;

    private LocalDateTime cancelledAt;

    private String cancellationReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Booking() {}

    @PrePersist
    protected void onCreate() {
        this.bookingReference = generateBookingReference();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String generateBookingReference() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public void complete() {
        if (this.status == BookingStatus.CONFIRMED) {
            this.status = BookingStatus.COMPLETED;
        }
    }

    public boolean isPending() {
        return status == BookingStatus.PENDING;
    }

    public boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED;
    }

    public boolean isCancelled() {
        return status == BookingStatus.CANCELLED;
    }

    public boolean isCompleted() {
        return status == BookingStatus.COMPLETED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public Integer getNumberOfRooms() { return numberOfRooms; }
    public void setNumberOfRooms(Integer numberOfRooms) { this.numberOfRooms = numberOfRooms; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public long getNights() {
        return checkInDate != null && checkOutDate != null ?
                java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate) : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking booking)) return false;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", reference='" + bookingReference + '\'' +
                ", status=" + status +
                '}';
    }
}
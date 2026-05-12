package com.example.hotelproject.booking.dto;

import com.example.hotelproject.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingResponseDto {

    private final Long id;
    private final String bookingReference;
    private final Long guestId;
    private final String guestName;
    private final String guestEmail;
    private final Long roomTypeId;
    private final String roomTypeName;
    private final Long hotelId;
    private final String hotelName;
    private final Integer numberOfRooms;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final long nights;
    private final Integer numberOfGuests;
    private final BookingStatus status;
    private final BigDecimal totalPrice;
    private final BigDecimal paidAmount;
    private final BigDecimal remainingAmount;
    private final String specialRequests;
    private final LocalDateTime confirmedAt;
    private final LocalDateTime cancelledAt;
    private final String cancellationReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BookingResponseDto(Long id, String bookingReference, Long guestId,
                              String guestName, String guestEmail, Long roomTypeId,
                              String roomTypeName, Long hotelId, String hotelName,
                              Integer numberOfRooms, LocalDate checkInDate,
                              LocalDate checkOutDate, long nights, Integer numberOfGuests,
                              BookingStatus status, BigDecimal totalPrice,
                              BigDecimal paidAmount, String specialRequests,
                              LocalDateTime confirmedAt, LocalDateTime cancelledAt,
                              String cancellationReason, LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
        this.id = id;
        this.bookingReference = bookingReference;
        this.guestId = guestId;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.numberOfRooms = numberOfRooms;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.nights = nights;
        this.numberOfGuests = numberOfGuests;
        this.status = status;
        this.totalPrice = totalPrice;
        this.paidAmount = paidAmount;
        this.remainingAmount = totalPrice.subtract(paidAmount);
        this.specialRequests = specialRequests;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getBookingReference() { return bookingReference; }
    public Long getGuestId() { return guestId; }
    public String getGuestName() { return guestName; }
    public String getGuestEmail() { return guestEmail; }
    public Long getRoomTypeId() { return roomTypeId; }
    public String getRoomTypeName() { return roomTypeName; }
    public Long getHotelId() { return hotelId; }
    public String getHotelName() { return hotelName; }
    public Integer getNumberOfRooms() { return numberOfRooms; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public long getNights() { return nights; }
    public Integer getNumberOfGuests() { return numberOfGuests; }
    public BookingStatus getStatus() { return status; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public String getSpecialRequests() { return specialRequests; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public String getCancellationReason() { return cancellationReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
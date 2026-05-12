package com.example.hotelproject.booking.dto;

import com.example.hotelproject.enums.BookingStatus;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class BookingSearchRequestDto {

    private Long guestId;

    private Long hotelId;

    private BookingStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private String bookingReference;

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
}
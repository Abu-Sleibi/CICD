package com.example.hotelproject.booking.service;

import com.example.hotelproject.booking.dto.*;
import com.example.hotelproject.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto request);

    BookingResponseDto getBookingById(Long id);

    BookingResponseDto getBookingByReference(String reference);

    Page<BookingResponseDto> getGuestBookings(Long guestId, Pageable pageable);

    Page<BookingResponseDto> getGuestBookingsByStatus(Long guestId, BookingStatus status, Pageable pageable);

    Page<BookingResponseDto> getHotelBookings(Long hotelId, Pageable pageable);

    Page<BookingResponseDto> getHotelBookingsByStatus(Long hotelId, BookingStatus status, Pageable pageable);

    BookingResponseDto confirmBooking(Long bookingId);

    BookingResponseDto cancelBooking(Long bookingId, CancelBookingRequestDto request);

    BookingResponseDto updatePaymentStatus(Long bookingId, BigDecimal amountPaid);

    boolean checkRoomAvailability(Long roomTypeId, LocalDate checkIn, LocalDate checkOut,
                                  Integer numberOfRooms, Long currentBookingId);

    Page<BookingResponseDto> searchBookings(BookingSearchRequestDto searchRequest, Pageable pageable);

    BookingResponseDto updateBooking(Long id, BookingRequestDto request);

    HotelStatisticsDto getHotelStatistics(Long hotelId);

    // FIX: 10 Upcoming confirmed bookings for a specific hotel (checkIn >= today)
    Page<BookingResponseDto> getUpcomingHotelBookings(Long hotelId, Pageable pageable);

    BookingResponseDto completeBooking(Long bookingId);

    BookingResponseDto markNoShow(Long bookingId);

    Page<BookingResponseDto> getMyBookings(String guestEmail, Pageable pageable);
}
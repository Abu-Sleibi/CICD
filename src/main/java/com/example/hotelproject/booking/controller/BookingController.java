package com.example.hotelproject.booking.controller;

import com.example.hotelproject.auth.security.CurrentUser;
import com.example.hotelproject.booking.dto.*;
import com.example.hotelproject.booking.service.BookingService;
import com.example.hotelproject.enums.BookingStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "05. Booking Management", description = "Create, confirm, cancel, and search bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody BookingRequestDto request,
            UriComponentsBuilder uriBuilder) {

        BookingResponseDto response = bookingService.createBooking(request);

        URI location = uriBuilder
                .path("/api/v1/bookings/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BookingResponseDto>> getMyBookings(
            @CurrentUser UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<BookingResponseDto> bookings = bookingService.getMyBookings(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_MANAGER') or @bookingSecurity.isBookingOwner(#id)")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable Long id) {
        BookingResponseDto response = bookingService.getBookingById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_MANAGER') or @bookingSecurity.isBookingOwnerByReference(#reference)")
    public ResponseEntity<BookingResponseDto> getBookingByReference(@PathVariable String reference) {
        BookingResponseDto response = bookingService.getBookingByReference(reference);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/guests/{guestId}")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurity.isCurrentUserGuest(#guestId)")
    public ResponseEntity<Page<BookingResponseDto>> getGuestBookings(
            @PathVariable Long guestId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<BookingResponseDto> bookings = bookingService.getGuestBookings(guestId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/guests/{guestId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurity.isCurrentUserGuest(#guestId)")
    public ResponseEntity<Page<BookingResponseDto>> getGuestBookingsByStatus(
            @PathVariable Long guestId,
            @PathVariable BookingStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<BookingResponseDto> bookings = bookingService.getGuestBookingsByStatus(guestId, status, pageable);
        return ResponseEntity.ok(bookings);
    }

    // FIX: 8 Ownership enforced via hotelSecurity.isManagerOfHotel — managers can only read their own hotel's bookings
    @GetMapping("/hotels/{hotelId}")
    @PreAuthorize("hasRole('ADMIN') or @hotelSecurity.isManagerOfHotel(#hotelId)")
    public ResponseEntity<Page<BookingResponseDto>> getHotelBookings(
            @PathVariable Long hotelId,
            @PageableDefault(size = 20, sort = "checkInDate", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<BookingResponseDto> bookings = bookingService.getHotelBookings(hotelId, pageable);
        return ResponseEntity.ok(bookings);
    }

    // FIX: 8 Ownership enforced via hotelSecurity.isManagerOfHotel
    @GetMapping("/hotels/{hotelId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or @hotelSecurity.isManagerOfHotel(#hotelId)")
    public ResponseEntity<Page<BookingResponseDto>> getHotelBookingsByStatus(
            @PathVariable Long hotelId,
            @PathVariable BookingStatus status,
            @PageableDefault(size = 20, sort = "checkInDate", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<BookingResponseDto> bookings = bookingService.getHotelBookingsByStatus(hotelId, status, pageable);
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<BookingResponseDto> confirmBooking(@PathVariable Long id) {
        BookingResponseDto response = bookingService.confirmBooking(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER') or @bookingSecurity.isBookingOwner(#id)")
    public ResponseEntity<BookingResponseDto> cancelBooking(
            @PathVariable Long id,
            @Valid @RequestBody CancelBookingRequestDto request) {

        BookingResponseDto response = bookingService.cancelBooking(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurity.isBookingOwner(#id)")
    public ResponseEntity<BookingResponseDto> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequestDto request) {

        BookingResponseDto response = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_MANAGER')")
    public ResponseEntity<Page<BookingResponseDto>> searchBookings(
            @ModelAttribute BookingSearchRequestDto searchRequest,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<BookingResponseDto> bookings = bookingService.searchBookings(searchRequest, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/check-availability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AvailabilityCheckResponse> checkAvailability(
            @RequestParam Long roomTypeId,
            @RequestParam String checkInDate,
            @RequestParam String checkOutDate,
            @RequestParam(defaultValue = "1") Integer numberOfRooms,
            @RequestParam(required = false) Long currentBookingId) {

        LocalDate checkIn = LocalDate.parse(checkInDate);
        LocalDate checkOut = LocalDate.parse(checkOutDate);

        boolean available = bookingService.checkRoomAvailability(
                roomTypeId, checkIn, checkOut, numberOfRooms, currentBookingId);

        return ResponseEntity.ok(new AvailabilityCheckResponse(available));
    }

    static class AvailabilityCheckResponse {
        private final boolean available;
        public AvailabilityCheckResponse(boolean available) { this.available = available; }
        public boolean isAvailable() { return available; }
    }

    @GetMapping("/hotels/{hotelId}/statistics")
    @PreAuthorize("hasRole('ADMIN') or @hotelSecurity.isManagerOfHotel(#hotelId)")
    public ResponseEntity<HotelStatisticsDto> getHotelStatistics(@PathVariable Long hotelId) {
        HotelStatisticsDto statistics = bookingService.getHotelStatistics(hotelId);
        return ResponseEntity.ok(statistics);
    }

    // FIX: 10 Upcoming bookings endpoint — CONFIRMED bookings with checkIn >= today
    @GetMapping("/hotel/{hotelId}/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<Page<BookingResponseDto>> getUpcomingHotelBookings(
            @PathVariable Long hotelId,
            @PageableDefault(size = 20, sort = "checkInDate", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<BookingResponseDto> bookings = bookingService.getUpcomingHotelBookings(hotelId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<BookingResponseDto> completeBooking(@PathVariable Long id) {
        BookingResponseDto response = bookingService.completeBooking(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<BookingResponseDto> markNoShow(@PathVariable Long id) {
        BookingResponseDto response = bookingService.markNoShow(id);
        return ResponseEntity.ok(response);
    }
}
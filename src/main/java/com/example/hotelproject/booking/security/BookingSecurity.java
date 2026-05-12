package com.example.hotelproject.booking.security;

import com.example.hotelproject.auth.entity.User;
import com.example.hotelproject.auth.repository.UserRepository;
import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.booking.entity.Guest;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.booking.repository.GuestRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("bookingSecurity")
public class BookingSecurity {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final GuestRepository guestRepository;

    public BookingSecurity(BookingRepository bookingRepository,
                           UserRepository userRepository,
                           GuestRepository guestRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.guestRepository = guestRepository;
    }

    public boolean isBookingOwner(Long bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            return false;
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (currentUser == null) {
            return false;
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);

        if (booking == null) {
            return false;
        }

        return booking.getGuest().getEmail().equals(currentUser.getEmail());
    }

    public boolean isBookingOwnerByReference(String bookingReference) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            return false;
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (currentUser == null) {
            return false;
        }

        Booking booking = bookingRepository.findByBookingReference(bookingReference).orElse(null);

        if (booking == null) {
            return false;
        }

        return booking.getGuest().getEmail().equals(currentUser.getEmail());
    }

    public boolean isCurrentUserGuest(Long guestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            return false;
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (currentUser == null) {
            return false;
        }

        Guest guest = guestRepository.findByEmail(currentUser.getEmail()).orElse(null);

        if (guest == null) {
            return false;
        }

        return guest.getId().equals(guestId);
    }
}
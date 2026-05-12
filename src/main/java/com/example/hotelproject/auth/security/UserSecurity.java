package com.example.hotelproject.auth.security;

import com.example.hotelproject.auth.entity.Role;
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

@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    // FIX: 4 Added GuestRepository to correctly resolve guest ownership
    private final GuestRepository guestRepository;

    public UserSecurity(UserRepository userRepository, BookingRepository bookingRepository,
                        GuestRepository guestRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.guestRepository = guestRepository;
    }

    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userRepository.findByUsername(username).orElse(null);

            return user != null && user.getId().equals(userId);
        }

        return false;
    }

    public boolean isCurrentUserByBooking(Long bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                Booking booking = bookingRepository.findById(bookingId).orElse(null);
                return booking != null && booking.getGuest().getEmail().equals(user.getEmail());
            }
        }

        return false;
    }

    public boolean isCurrentUserByBookingReference(String bookingReference) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                Booking booking = bookingRepository.findByBookingReference(bookingReference).orElse(null);
                return booking != null && booking.getGuest().getEmail().equals(user.getEmail());
            }
        }

        return false;
    }

    // FIX: 4 Previously compared User.id with Guest.id (different entity IDs).
    // Now loads the Guest entity and compares email with the authenticated user's email.
    public boolean isCurrentUserByGuestId(Long guestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                Guest guest = guestRepository.findById(guestId).orElse(null);
                return guest != null && guest.getEmail().equals(user.getEmail());
            }
        }

        return false;
    }

    public boolean isManagerOfHotel(Long hotelId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userRepository.findByUsername(username).orElse(null);

            return user != null &&
                    user.getRoles().contains(Role.HOTEL_MANAGER) &&
                    hotelId.equals(user.getHotelId());
        }

        return false;
    }
}
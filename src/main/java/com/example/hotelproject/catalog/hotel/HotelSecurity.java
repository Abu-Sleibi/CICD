package com.example.hotelproject.catalog.hotel;

import com.example.hotelproject.auth.entity.Role;
import com.example.hotelproject.auth.entity.User;
import com.example.hotelproject.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("hotelSecurity")
public class HotelSecurity {

    private final UserRepository userRepository;

    public HotelSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isManagerOfHotel(Long hotelId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            return false;
        }

        String username = ((UserDetails) principal).getUsername();
        User user = userRepository.findByUsername(username).orElse(null);

        return user != null &&
                user.getRoles().contains(Role.HOTEL_MANAGER) &&
                hotelId.equals(user.getHotelId());
    }
}
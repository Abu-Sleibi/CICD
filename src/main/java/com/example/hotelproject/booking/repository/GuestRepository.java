package com.example.hotelproject.booking.repository;

import com.example.hotelproject.booking.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    Optional<Guest> findByEmail(String email);
    boolean existsByEmail(String email);
}
package com.example.hotelproject.availability.repository;

import com.example.hotelproject.availability.entity.RoomAvailability;
import com.example.hotelproject.catalog.room.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {

    Optional<RoomAvailability> findByRoomTypeIdAndDate(Long roomTypeId, LocalDate date);

    List<RoomAvailability> findByRoomTypeIdAndDateBetween(Long roomTypeId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.roomType.id = :roomTypeId " +
            "AND ra.date BETWEEN :startDate AND :endDate ORDER BY ra.date")
    List<RoomAvailability> findAndLockByRoomTypeIdAndDateBetween(
            @Param("roomTypeId") Long roomTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.roomType.hotel.id = :hotelId " +
            "AND ra.date BETWEEN :startDate AND :endDate")
    List<RoomAvailability> findByHotelIdAndDateBetween(
            @Param("hotelId") Long hotelId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.roomType = :roomType " +
            "AND ra.date >= :startDate ORDER BY ra.date")
    List<RoomAvailability> findByRoomTypeAndDateAfter(
            @Param("roomType") RoomType roomType,
            @Param("startDate") LocalDate startDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.roomType.id = :roomTypeId AND ra.date = :date")
    Optional<RoomAvailability> findByRoomTypeIdAndDateWithLock(
            @Param("roomTypeId") Long roomTypeId,
            @Param("date") LocalDate date);

    void deleteByRoomTypeIdAndDateBetween(Long roomTypeId, LocalDate startDate, LocalDate endDate);
}
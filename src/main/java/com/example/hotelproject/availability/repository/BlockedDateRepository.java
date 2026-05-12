package com.example.hotelproject.availability.repository;

import com.example.hotelproject.availability.entity.BlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BlockedDateRepository extends JpaRepository<BlockedDate, Long> {

    List<BlockedDate> findByHotelIdAndActiveTrue(Long hotelId);

    List<BlockedDate> findByRoomTypeIdAndActiveTrue(Long roomTypeId);

    @Query("SELECT bd FROM BlockedDate bd WHERE bd.active = true AND " +
            "bd.hotel.id = :hotelId AND " +
            "bd.startDate <= :endDate AND bd.endDate >= :startDate")
    List<BlockedDate> findActiveBlocksInDateRange(
            @Param("hotelId") Long hotelId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT bd FROM BlockedDate bd WHERE bd.active = true AND " +
            "(bd.roomType.id = :roomTypeId OR bd.roomType IS NULL) AND " +
            "bd.startDate <= :date AND bd.endDate >= :date")
    List<BlockedDate> findActiveBlocksForDateAndRoomType(
            @Param("roomTypeId") Long roomTypeId,
            @Param("date") LocalDate date);

    @Query("SELECT CASE WHEN COUNT(bd) > 0 THEN true ELSE false END FROM BlockedDate bd " +
            "WHERE bd.active = true AND " +
            "(bd.roomType.id = :roomTypeId OR bd.roomType IS NULL) AND " +
            "bd.startDate <= :date AND bd.endDate >= :date")
    boolean isDateBlocked(
            @Param("roomTypeId") Long roomTypeId,
            @Param("date") LocalDate date);
}
package com.example.hotelproject.catalog.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    Optional<RoomType> findByIdAndActiveTrue(Long id);

    List<RoomType> findByHotelIdAndActiveTrue(Long hotelId);

    List<RoomType> findByHotelId(Long hotelId);

    boolean existsByNameAndHotelId(String name, Long hotelId);

    boolean existsByNameAndHotelIdAndIdNot(String name, Long hotelId, Long id);
}
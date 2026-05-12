package com.example.hotelproject.catalog.hotel;

import com.example.hotelproject.catalog.room.RoomTypeRequestDto;
import com.example.hotelproject.catalog.room.RoomTypeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface HotelService {

    // Hotel CRUD operations
    HotelResponseDto createHotel(HotelRequestDto hotelRequest);
    HotelResponseDto getHotelById(Long id);
    Page<HotelResponseDto> getAllHotels(Pageable pageable);
    HotelResponseDto updateHotel(Long id, HotelRequestDto hotelRequest);
    void deleteHotel(Long id);
    void activateHotel(Long id);

    // RoomType CRUD operations
    RoomTypeResponseDto createRoomType(RoomTypeRequestDto roomTypeRequest);
    RoomTypeResponseDto getRoomTypeById(Long id);
    List<RoomTypeResponseDto> getRoomTypesByHotelId(Long hotelId);
    RoomTypeResponseDto updateRoomType(Long id, RoomTypeRequestDto roomTypeRequest);
    void deleteRoomType(Long id);

    // FIX: 9 Extended search with date range, capacity, price, and amenity filters
    Page<HotelResponseDto> searchHotels(String city, String country, Integer minRating,
                                        LocalDate checkIn, LocalDate checkOut, Integer guests,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        List<String> amenities, Pageable pageable);
    List<HotelResponseDto> getManagerHotels(String username);
    List<HotelResponseDto> getHotelsByCity(String city);
    List<HotelResponseDto> getHotelsByCountry(String country);
    List<HotelResponseDto> getHotelsByRating(Integer starRating);
}
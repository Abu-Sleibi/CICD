package com.example.hotelproject.catalog.hotel;

import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.catalog.room.RoomTypeRequestDto;
import com.example.hotelproject.catalog.room.RoomTypeResponseDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class HotelMapper {

    private final HotelRepository hotelRepository;

    public HotelMapper(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public Hotel toEntity(HotelRequestDto dto) {
        Hotel hotel = new Hotel();
        hotel.setName(dto.getName());
        hotel.setDescription(dto.getDescription());
        hotel.setAddress(dto.getAddress());
        hotel.setCity(dto.getCity());
        hotel.setCountry(dto.getCountry());
        hotel.setPostalCode(dto.getPostalCode());
        hotel.setPhone(dto.getPhone());
        hotel.setEmail(dto.getEmail());
        hotel.setStarRating(dto.getStarRating());
        return hotel;
    }

    public HotelResponseDto toDto(Hotel hotel) {
        return new HotelResponseDto(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getAddress(),
                hotel.getCity(),
                hotel.getCountry(),
                hotel.getPostalCode(),
                hotel.getPhone(),
                hotel.getEmail(),
                hotel.getStarRating(),
                hotel.isActive(),
                hotel.getCreatedAt(),
                hotel.getUpdatedAt(),
                hotel.getRoomTypes().stream()
                        .filter(RoomType::isActive)
                        .map(this::toRoomTypeDto)
                        .collect(Collectors.toList()),
                hotel.getImages()
        );
    }

    public RoomType toEntity(RoomTypeRequestDto dto) {
        RoomType roomType = new RoomType();
        roomType.setName(dto.getName());
        roomType.setDescription(dto.getDescription());
        roomType.setCapacity(dto.getCapacity());
        roomType.setBasePrice(dto.getBasePrice());
        roomType.setAmenities(dto.getAmenities());
        roomType.setTotalRooms(dto.getTotalRooms());

        hotelRepository.findById(dto.getHotelId())
                .ifPresent(roomType::setHotel);

        return roomType;
    }

    public RoomTypeResponseDto toRoomTypeDto(RoomType roomType) {
        return new RoomTypeResponseDto(
                roomType.getId(),
                roomType.getName(),
                roomType.getDescription(),
                roomType.getCapacity(),
                roomType.getBasePrice(),
                roomType.getAmenities(),
                roomType.getTotalRooms(),
                roomType.isActive(),
                roomType.getHotel().getId(),
                roomType.getHotel().getName()
        );
    }
}
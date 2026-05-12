package com.example.hotelproject.booking.mapper;

import com.example.hotelproject.booking.dto.*;
import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.booking.entity.Guest;
import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.catalog.room.RoomTypeRepository;
import com.example.hotelproject.catalog.room.RoomTypeNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    private final RoomTypeRepository roomTypeRepository;

    public BookingMapper(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    public Guest toEntity(GuestDto dto) {
        Guest guest = new Guest();
        guest.setFullName(dto.getFullName());
        guest.setEmail(dto.getEmail());
        guest.setPhone(dto.getPhone());
        guest.setAddress(dto.getAddress());
        return guest;
    }

    public GuestDto toGuestDto(Guest guest) {
        GuestDto dto = new GuestDto();
        dto.setFullName(guest.getFullName());
        dto.setEmail(guest.getEmail());
        dto.setPhone(guest.getPhone());
        dto.setAddress(guest.getAddress());
        return dto;
    }

    public Booking toEntity(BookingRequestDto dto, Guest guest) {
        Booking booking = new Booking();
        booking.setGuest(guest);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setNumberOfGuests(dto.getNumberOfGuests());
        booking.setNumberOfRooms(dto.getNumberOfRooms());
        booking.setSpecialRequests(dto.getSpecialRequests());

        RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeId())
                .orElseThrow(() -> new RoomTypeNotFoundException(dto.getRoomTypeId()));
        booking.setRoomType(roomType);

        return booking;
    }

    public BookingResponseDto toDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getBookingReference(),
                booking.getGuest().getId(),
                booking.getGuest().getFullName(),
                booking.getGuest().getEmail(),
                booking.getRoomType().getId(),
                booking.getRoomType().getName(),
                booking.getRoomType().getHotel().getId(),
                booking.getRoomType().getHotel().getName(),
                booking.getNumberOfRooms(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNights(),
                booking.getNumberOfGuests(),
                booking.getStatus(),
                booking.getTotalPrice(),
                booking.getPaidAmount(),
                booking.getSpecialRequests(),
                booking.getConfirmedAt(),
                booking.getCancelledAt(),
                booking.getCancellationReason(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
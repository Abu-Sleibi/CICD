package com.example.hotelproject.catalog.hotel;

import com.example.hotelproject.auth.entity.Role;
import com.example.hotelproject.auth.entity.User;
import com.example.hotelproject.auth.repository.UserRepository;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.enums.BookingStatus;
import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.catalog.room.RoomTypeNotFoundException;
import com.example.hotelproject.catalog.room.RoomTypeRepository;
import com.example.hotelproject.catalog.room.RoomTypeRequestDto;
import com.example.hotelproject.catalog.room.RoomTypeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelMapper hotelMapper;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public HotelServiceImpl(HotelRepository hotelRepository,
                            RoomTypeRepository roomTypeRepository,
                            HotelMapper hotelMapper,
                            BookingRepository bookingRepository,
                            UserRepository userRepository) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.hotelMapper = hotelMapper;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Override
    public HotelResponseDto createHotel(HotelRequestDto hotelRequest) {
        if (hotelRepository.existsByNameAndCity(hotelRequest.getName(), hotelRequest.getCity())) {
            throw new IllegalArgumentException("Hotel with name '" + hotelRequest.getName() +
                    "' already exists in city '" + hotelRequest.getCity() + "'");
        }

        if (hotelRequest.getEmail() != null && hotelRepository.existsByEmail(hotelRequest.getEmail())) {
            throw new IllegalArgumentException("Hotel with email '" + hotelRequest.getEmail() + "' already exists");
        }

        Hotel hotel = hotelMapper.toEntity(hotelRequest);
        Hotel savedHotel = hotelRepository.save(hotel);

        return hotelMapper.toDto(savedHotel);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponseDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new HotelNotFoundException(id));
        return hotelMapper.toDto(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponseDto> getAllHotels(Pageable pageable) {
        return hotelRepository.findAllByActiveTrue(pageable)
                .map(hotelMapper::toDto);
    }

    @Override
    public HotelResponseDto updateHotel(Long id, HotelRequestDto hotelRequest) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));

        if (hotelRequest.getEmail() != null &&
                !hotelRequest.getEmail().equals(hotel.getEmail()) &&
                hotelRepository.existsByEmailAndIdNot(hotelRequest.getEmail(), id)) {
            throw new IllegalArgumentException("Hotel with email '" + hotelRequest.getEmail() + "' already exists");
        }

        hotel.setName(hotelRequest.getName());
        hotel.setDescription(hotelRequest.getDescription());
        hotel.setAddress(hotelRequest.getAddress());
        hotel.setCity(hotelRequest.getCity());
        hotel.setCountry(hotelRequest.getCountry());
        hotel.setPostalCode(hotelRequest.getPostalCode());
        hotel.setPhone(hotelRequest.getPhone());
        hotel.setEmail(hotelRequest.getEmail());
        hotel.setStarRating(hotelRequest.getStarRating());

        Hotel updatedHotel = hotelRepository.save(hotel);
        return hotelMapper.toDto(updatedHotel);
    }

    @Override
    public void deleteHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));

        hotel.setActive(false);
        hotelRepository.save(hotel);

        List<RoomType> roomTypes = roomTypeRepository.findByHotelId(id);
        roomTypes.forEach(rt -> rt.setActive(false));
        roomTypeRepository.saveAll(roomTypes);
    }

    @Override
    public void activateHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));

        hotel.setActive(true);
        hotelRepository.save(hotel);
    }

    @Override
    public RoomTypeResponseDto createRoomType(RoomTypeRequestDto roomTypeRequest) {
        Hotel hotel = hotelRepository.findByIdAndActiveTrue(roomTypeRequest.getHotelId())
                .orElseThrow(() -> new HotelNotFoundException(roomTypeRequest.getHotelId()));

        if (roomTypeRepository.existsByNameAndHotelId(roomTypeRequest.getName(), roomTypeRequest.getHotelId())) {
            throw new IllegalArgumentException("Room type with name '" + roomTypeRequest.getName() +
                    "' already exists in hotel '" + hotel.getName() + "'");
        }

        RoomType roomType = hotelMapper.toEntity(roomTypeRequest);
        roomType.setHotel(hotel);

        RoomType savedRoomType = roomTypeRepository.save(roomType);
        return hotelMapper.toRoomTypeDto(savedRoomType);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponseDto getRoomTypeById(Long id) {
        RoomType roomType = roomTypeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));
        return hotelMapper.toRoomTypeDto(roomType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeResponseDto> getRoomTypesByHotelId(Long hotelId) {
        return roomTypeRepository.findByHotelIdAndActiveTrue(hotelId)
                .stream()
                .map(hotelMapper::toRoomTypeDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoomTypeResponseDto updateRoomType(Long id, RoomTypeRequestDto roomTypeRequest) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        if (!roomTypeRequest.getName().equals(roomType.getName())) {
            if (roomTypeRepository.existsByNameAndHotelIdAndIdNot(
                    roomTypeRequest.getName(), roomTypeRequest.getHotelId(), id)) {
                throw new IllegalArgumentException("Room type with name '" + roomTypeRequest.getName() +
                        "' already exists in this hotel");
            }
        }

        roomType.setName(roomTypeRequest.getName());
        roomType.setDescription(roomTypeRequest.getDescription());
        roomType.setCapacity(roomTypeRequest.getCapacity());
        roomType.setBasePrice(roomTypeRequest.getBasePrice());
        roomType.setAmenities(roomTypeRequest.getAmenities());
        roomType.setTotalRooms(roomTypeRequest.getTotalRooms());

        if (!roomTypeRequest.getHotelId().equals(roomType.getHotel().getId())) {
            Hotel hotel = hotelRepository.findByIdAndActiveTrue(roomTypeRequest.getHotelId())
                    .orElseThrow(() -> new HotelNotFoundException(roomTypeRequest.getHotelId()));
            roomType.setHotel(hotel);
        }

        RoomType updatedRoomType = roomTypeRepository.save(roomType);
        return hotelMapper.toRoomTypeDto(updatedRoomType);
    }

    @Override
    public void deleteRoomType(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RoomTypeNotFoundException(id));

        if (bookingRepository.existsByRoomTypeIdAndStatusIn(
                id, List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED))) {
            throw new IllegalArgumentException("Cannot delete room type with active bookings");
        }

        roomType.setActive(false);
        roomTypeRepository.save(roomType);
    }

    // FIX: 9 Extended search supporting date range, guest capacity, price range, and amenities.
    // Base city/country/rating filter is pushed to the DB; remaining filters run in Java.
    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponseDto> searchHotels(String city, String country, Integer minRating,
                                               LocalDate checkIn, LocalDate checkOut, Integer guests,
                                               BigDecimal minPrice, BigDecimal maxPrice,
                                               List<String> amenities, Pageable pageable) {
        List<Hotel> baseHotels;
        if (amenities != null && !amenities.isEmpty()) {
            baseHotels = hotelRepository.searchHotelsWithAmenity(amenities.get(0), city, country, minRating);
        } else {
            baseHotels = hotelRepository.searchHotels(city, country, minRating, Pageable.unpaged()).getContent();
        }

        List<HotelResponseDto> filtered = baseHotels.stream()
                .filter(h -> matchesGuests(h, guests))
                .filter(h -> matchesPrice(h, minPrice, maxPrice))
                .filter(h -> matchesAmenities(h, amenities))
                .filter(h -> matchesDateAvailability(h, checkIn, checkOut, guests))
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<HotelResponseDto> pageContent = start >= filtered.size() ? List.of() : filtered.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    private boolean matchesGuests(Hotel hotel, Integer guests) {
        if (guests == null) return true;
        return hotel.getRoomTypes().stream()
                .anyMatch(rt -> rt.isActive() && rt.getCapacity() >= guests);
    }

    private boolean matchesPrice(Hotel hotel, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null) return true;
        return hotel.getRoomTypes().stream()
                .filter(RoomType::isActive)
                .anyMatch(rt -> {
                    boolean aboveMin = minPrice == null || rt.getBasePrice().compareTo(minPrice) >= 0;
                    boolean belowMax = maxPrice == null || rt.getBasePrice().compareTo(maxPrice) <= 0;
                    return aboveMin && belowMax;
                });
    }

    private boolean matchesAmenities(Hotel hotel, List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) return true;
        return hotel.getRoomTypes().stream()
                .filter(RoomType::isActive)
                .anyMatch(rt -> rt.getAmenities() != null && rt.getAmenities().containsAll(amenities));
    }

    private boolean matchesDateAvailability(Hotel hotel, LocalDate checkIn, LocalDate checkOut, Integer guests) {
        if (checkIn == null || checkOut == null) return true;
        return hotel.getRoomTypes().stream()
                .filter(RoomType::isActive)
                .filter(rt -> guests == null || rt.getCapacity() >= guests)
                .anyMatch(rt -> {
                    Integer booked = bookingRepository.sumBookedRoomsForRoomType(rt.getId(), checkIn, checkOut);
                    return rt.getTotalRooms() > (booked != null ? booked : 0);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDto> getManagerHotels(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (user.getRoles().contains(Role.ADMIN)) {
            return hotelRepository.findAllByActiveTrue(Pageable.unpaged())
                    .stream()
                    .map(hotelMapper::toDto)
                    .collect(Collectors.toList());
        }

        if (user.getHotelId() == null) {
            return List.of();
        }
        return hotelRepository.findByIdAndActiveTrue(user.getHotelId())
                .map(hotel -> List.of(hotelMapper.toDto(hotel)))
                .orElse(List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDto> getHotelsByCity(String city) {
        return hotelRepository.findByCityAndActiveTrue(city)
                .stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDto> getHotelsByCountry(String country) {
        return hotelRepository.findByCountryAndActiveTrue(country)
                .stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDto> getHotelsByRating(Integer starRating) {
        return hotelRepository.findByStarRatingAndActiveTrue(starRating)
                .stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }
}
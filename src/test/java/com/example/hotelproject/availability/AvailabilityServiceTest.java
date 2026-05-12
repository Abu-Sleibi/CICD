package com.example.hotelproject.availability;

import com.example.hotelproject.availability.dto.AvailabilityRequestDto;
import com.example.hotelproject.availability.dto.AvailabilityResponseDto;
import com.example.hotelproject.availability.dto.PriceCalculationRequestDto;
import com.example.hotelproject.availability.dto.PriceCalculationResponseDto;
import com.example.hotelproject.availability.entity.RoomAvailability;
import com.example.hotelproject.availability.exception.InvalidDateRangeException;
import com.example.hotelproject.availability.exception.RoomNotAvailableException;
import com.example.hotelproject.availability.mapper.AvailabilityMapper;
import com.example.hotelproject.availability.repository.BlockedDateRepository;
import com.example.hotelproject.availability.repository.PricingRuleRepository;
import com.example.hotelproject.availability.repository.RoomAvailabilityRepository;
import com.example.hotelproject.availability.service.AvailabilityServiceImpl;
import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.catalog.hotel.Hotel;
import com.example.hotelproject.catalog.hotel.HotelRepository;
import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.catalog.room.RoomTypeRepository;
import com.example.hotelproject.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AvailabilityServiceTest {

    @Mock
    private RoomAvailabilityRepository availabilityRepository;

    @Mock
    private PricingRuleRepository pricingRuleRepository;

    @Mock
    private BlockedDateRepository blockedDateRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AvailabilityMapper availabilityMapper;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    private Hotel hotel;
    private RoomType roomType;
    private RoomAvailability roomAvailability;
    private Booking booking;
    private AvailabilityRequestDto availabilityRequest;
    private PriceCalculationRequestDto priceRequest;
    private AvailabilityResponseDto availabilityResponse;
    private PriceCalculationResponseDto priceResponse;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Hotel");

        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe Room");
        roomType.setCapacity(2);
        roomType.setBasePrice(new BigDecimal("150.00"));
        roomType.setTotalRooms(20);
        roomType.setHotel(hotel);

        roomAvailability = new RoomAvailability();
        roomAvailability.setId(1L);
        roomAvailability.setRoomType(roomType);
        roomAvailability.setDate(LocalDate.now().plusDays(5));
        roomAvailability.setTotalRooms(20);
        roomAvailability.setBookedRooms(0);
        roomAvailability.setHeldRooms(0);
        roomAvailability.setBlockedRooms(0);

        booking = new Booking();
        booking.setId(1L);
        booking.setRoomType(roomType);
        booking.setCheckInDate(LocalDate.now().plusDays(5));
        booking.setCheckOutDate(LocalDate.now().plusDays(8));
        booking.setNumberOfRooms(1);
        booking.setStatus(BookingStatus.CONFIRMED);

        availabilityRequest = new AvailabilityRequestDto();
        availabilityRequest.setHotelId(1L);
        availabilityRequest.setRoomTypeId(1L);
        availabilityRequest.setCheckInDate(LocalDate.now().plusDays(5));
        availabilityRequest.setCheckOutDate(LocalDate.now().plusDays(8));
        availabilityRequest.setGuests(2);
        availabilityRequest.setRoomsRequested(1);

        priceRequest = new PriceCalculationRequestDto();
        priceRequest.setRoomTypeId(1L);
        priceRequest.setCheckInDate(LocalDate.now().plusDays(5));
        priceRequest.setCheckOutDate(LocalDate.now().plusDays(8));
        priceRequest.setNumberOfRooms(1);

        availabilityResponse = mock(AvailabilityResponseDto.class);
        priceResponse = mock(PriceCalculationResponseDto.class);
    }

    @Test
    void checkAvailability_Success() {
        when(hotelRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(roomType));
        when(availabilityRepository.findByRoomTypeIdAndDateBetween(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any(), anyLong()))
                .thenReturn(new ArrayList<>());
        when(blockedDateRepository.isDateBlocked(anyLong(), any())).thenReturn(false);
        when(availabilityMapper.toAvailabilityResponseDto(any(), any(), any(), any(), any()))
                .thenReturn(availabilityResponse);

        AvailabilityResponseDto result = availabilityService.checkAvailability(availabilityRequest);

        assertThat(result).isNotNull();
    }

    @Test
    void checkAvailability_ReturnsCorrectAvailableRooms_WhenBookingsExist() {
        List<Booking> conflictingBookings = new ArrayList<>();
        conflictingBookings.add(booking);

        when(hotelRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(roomType));
        when(availabilityRepository.findByRoomTypeIdAndDateBetween(anyLong(), any(), any()))
                .thenReturn(List.of(roomAvailability));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any(), anyLong()))
                .thenReturn(conflictingBookings);
        when(blockedDateRepository.isDateBlocked(anyLong(), any())).thenReturn(false);
        when(availabilityMapper.toAvailabilityResponseDto(any(), any(), any(), any(), any()))
                .thenReturn(availabilityResponse);

        AvailabilityResponseDto result = availabilityService.checkAvailability(availabilityRequest);

        assertThat(result).isNotNull();
        verify(bookingRepository).findConflictingBookings(anyLong(), any(), any(), anyLong());
    }

    @Test
    void checkAvailability_ThrowsException_WhenHotelNotFound() {
        when(hotelRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());
        availabilityRequest.setHotelId(99L);

        assertThatThrownBy(() -> availabilityService.checkAvailability(availabilityRequest))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void checkAvailability_ThrowsException_WhenRoomTypeNotFound() {
        when(hotelRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());
        availabilityRequest.setRoomTypeId(99L);

        assertThatThrownBy(() -> availabilityService.checkAvailability(availabilityRequest))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void checkAvailability_ThrowsException_WhenRoomTypeNotBelongToHotel() {
        Hotel anotherHotel = new Hotel();
        anotherHotel.setId(2L);
        RoomType anotherRoomType = new RoomType();
        anotherRoomType.setId(2L);
        anotherRoomType.setHotel(anotherHotel);

        when(hotelRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(anotherRoomType));
        availabilityRequest.setRoomTypeId(2L);

        assertThatThrownBy(() -> availabilityService.checkAvailability(availabilityRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkAvailability_ThrowsException_WhenInvalidDateRange() {
        availabilityRequest.setCheckOutDate(availabilityRequest.getCheckInDate().minusDays(1));

        assertThatThrownBy(() -> availabilityService.checkAvailability(availabilityRequest))
                .isInstanceOf(InvalidDateRangeException.class);
    }

    @Test
    void checkAvailability_ThrowsException_WhenCheckInInPast() {
        availabilityRequest.setCheckInDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> availabilityService.checkAvailability(availabilityRequest))
                .isInstanceOf(InvalidDateRangeException.class);
    }

    @Test
    void calculatePrice_Success() {
        when(roomTypeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(roomType));
        when(pricingRuleRepository.findApplicableRulesForDate(anyLong(), anyLong(), any()))
                .thenReturn(new ArrayList<>());
        when(availabilityMapper.getDayType(any())).thenReturn(null);
        when(availabilityMapper.toPriceCalculationResponseDto(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(priceResponse);

        PriceCalculationResponseDto result = availabilityService.calculatePrice(priceRequest);

        assertThat(result).isNotNull();
    }

    @Test
    void calculatePrice_ThrowsException_WhenRoomTypeNotFound() {
        when(roomTypeRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());
        priceRequest.setRoomTypeId(99L);

        assertThatThrownBy(() -> availabilityService.calculatePrice(priceRequest))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void holdRooms_Success() {
        when(availabilityRepository.findByRoomTypeIdAndDateWithLock(eq(1L), any()))
                .thenReturn(Optional.of(roomAvailability));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any(), anyLong()))
                .thenReturn(new ArrayList<>());
        when(availabilityRepository.save(any(RoomAvailability.class))).thenReturn(roomAvailability);

        availabilityService.holdRooms(1L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), 1);

        verify(availabilityRepository, atLeastOnce()).save(any(RoomAvailability.class));
    }

    @Test
    void holdRooms_CreatesNewAvailability_WhenNotExists() {
        when(availabilityRepository.findByRoomTypeIdAndDateWithLock(eq(1L), any()))
                .thenReturn(Optional.empty());
        when(roomTypeRepository.getReferenceById(1L)).thenReturn(roomType);
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any(), anyLong()))
                .thenReturn(new ArrayList<>());
        when(availabilityRepository.save(any(RoomAvailability.class))).thenAnswer(i -> i.getArgument(0));

        availabilityService.holdRooms(1L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), 1);

        verify(availabilityRepository, atLeastOnce()).save(any(RoomAvailability.class));
    }

    @Test
    void holdRooms_ThrowsException_WhenNotAvailable() {
        roomAvailability.setBookedRooms(20);
        roomAvailability.setTotalRooms(20);

        when(availabilityRepository.findByRoomTypeIdAndDateWithLock(eq(1L), any()))
                .thenReturn(Optional.of(roomAvailability));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any(), anyLong()))
                .thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> availabilityService.holdRooms(1L,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough rooms available to hold");
    }

    @Test
    void holdRooms_ThrowsException_WhenConflictingBookingsExist() {
        roomAvailability.setTotalRooms(20);
        roomAvailability.setBookedRooms(0);
        roomAvailability.setHeldRooms(0);
        roomAvailability.setBlockedRooms(0);

        booking.setNumberOfRooms(20);
        List<Booking> conflictingBookings = new ArrayList<>();
        conflictingBookings.add(booking);

        when(availabilityRepository.findByRoomTypeIdAndDateWithLock(eq(1L), any()))
                .thenReturn(Optional.of(roomAvailability));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any(), anyLong()))
                .thenReturn(conflictingBookings);

        assertThatThrownBy(() -> availabilityService.holdRooms(1L,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), 1))
                .isInstanceOf(RoomNotAvailableException.class)
                .hasMessageContaining("Not enough rooms available");
    }

    @Test
    void confirmBooking_Success() {
        roomAvailability.setHeldRooms(1);

        when(availabilityRepository.findByRoomTypeIdAndDateWithLock(eq(1L), any()))
                .thenReturn(Optional.of(roomAvailability));
        when(availabilityRepository.save(any(RoomAvailability.class))).thenReturn(roomAvailability);

        availabilityService.confirmBooking(1L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), 1);

        verify(availabilityRepository, atLeastOnce()).save(any(RoomAvailability.class));
    }

    @Test
    void confirmBooking_ThrowsException_WhenNotEnoughHeldRooms() {
        roomAvailability.setHeldRooms(0);

        when(availabilityRepository.findByRoomTypeIdAndDateWithLock(eq(1L), any()))
                .thenReturn(Optional.of(roomAvailability));

        assertThatThrownBy(() -> availabilityService.confirmBooking(1L,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), 1))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void releaseHold_Success() {
        roomAvailability.setHeldRooms(5);

        when(availabilityRepository.findByRoomTypeIdAndDateWithLock(eq(1L), any()))
                .thenReturn(Optional.of(roomAvailability));
        when(availabilityRepository.save(any(RoomAvailability.class))).thenReturn(roomAvailability);

        availabilityService.releaseHold(1L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), 1);

        verify(availabilityRepository, atLeastOnce()).save(any(RoomAvailability.class));
    }

    @Test
    void cancelBooking_Success() {
        roomAvailability.setBookedRooms(5);

        when(availabilityRepository.findByRoomTypeIdAndDateWithLock(eq(1L), any()))
                .thenReturn(Optional.of(roomAvailability));
        when(availabilityRepository.save(any(RoomAvailability.class))).thenReturn(roomAvailability);

        availabilityService.cancelBooking(1L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), 1);

        verify(availabilityRepository, atLeastOnce()).save(any(RoomAvailability.class));
    }

    @Test
    void initializeRoomAvailability_Success() {
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(availabilityRepository.findByRoomTypeIdAndDate(anyLong(), any()))
                .thenReturn(Optional.empty());
        when(availabilityRepository.save(any(RoomAvailability.class))).thenReturn(roomAvailability);

        availabilityService.initializeRoomAvailability(1L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(10));

        verify(availabilityRepository, atLeast(5)).save(any(RoomAvailability.class));
    }
}
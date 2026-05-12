package com.example.hotelproject.booking;

import com.example.hotelproject.availability.dto.PriceCalculationResponseDto;
import com.example.hotelproject.availability.service.AvailabilityService;
import com.example.hotelproject.booking.dto.*;
import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.booking.entity.Guest;
import com.example.hotelproject.booking.exception.BookingNotFoundException;
import com.example.hotelproject.booking.exception.InvalidBookingStateException;
import com.example.hotelproject.booking.mapper.BookingMapper;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.booking.repository.GuestRepository;
import com.example.hotelproject.booking.service.BookingServiceImpl;
import com.example.hotelproject.catalog.hotel.Hotel;
import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.catalog.room.RoomTypeRepository;
import com.example.hotelproject.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Booking booking;
    private Guest guest;
    private RoomType roomType;
    private Hotel hotel;
    private BookingRequestDto bookingRequest;
    private BookingResponseDto bookingResponse;
    private GuestDto guestDto;
    private CancelBookingRequestDto cancelRequest;
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
        roomType.setTotalRooms(20);
        roomType.setBasePrice(new BigDecimal("150.00"));
        roomType.setHotel(hotel);
        roomType.setActive(true);

        guest = new Guest();
        guest.setId(1L);
        guest.setFullName("Test User");
        guest.setEmail("test@example.com");
        guest.setPhone("+962791234567");
        guest.setAddress("Amman, Jordan");

        booking = new Booking();
        booking.setId(1L);
        booking.setBookingReference("BK12345678");
        booking.setGuest(guest);
        booking.setRoomType(roomType);
        booking.setCheckInDate(LocalDate.now().plusDays(5));
        booking.setCheckOutDate(LocalDate.now().plusDays(8));
        booking.setNumberOfGuests(2);
        booking.setNumberOfRooms(1);
        booking.setTotalPrice(new BigDecimal("450.00"));
        booking.setPaidAmount(BigDecimal.ZERO);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        guestDto = new GuestDto();
        guestDto.setFullName("Test User");
        guestDto.setEmail("test@example.com");
        guestDto.setPhone("+962791234567");
        guestDto.setAddress("Amman, Jordan");

        bookingRequest = new BookingRequestDto();
        bookingRequest.setRoomTypeId(1L);
        bookingRequest.setCheckInDate(LocalDate.now().plusDays(5));
        bookingRequest.setCheckOutDate(LocalDate.now().plusDays(8));
        bookingRequest.setNumberOfGuests(2);
        bookingRequest.setNumberOfRooms(1);
        bookingRequest.setGuest(guestDto);
        bookingRequest.setSpecialRequests("Late check-in");

        bookingResponse = mock(BookingResponseDto.class);
        lenient().when(bookingResponse.getId()).thenReturn(1L);
        lenient().when(bookingResponse.getBookingReference()).thenReturn("BK12345678");
        lenient().when(bookingResponse.getStatus()).thenReturn(BookingStatus.PENDING);
        lenient().when(bookingResponse.getTotalPrice()).thenReturn(new BigDecimal("450.00"));

        cancelRequest = new CancelBookingRequestDto();
        cancelRequest.setReason("Change of plans");

        priceResponse = mock(PriceCalculationResponseDto.class);
        lenient().when(priceResponse.getTotalPrice()).thenReturn(new BigDecimal("450.00"));
    }

    @Test
    void createBooking_Success() {
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(guest));
        when(bookingRepository.findConflictingBookings(eq(1L), any(), any(), eq(-1L)))
                .thenReturn(new ArrayList<>());
        when(availabilityService.calculatePrice(any())).thenReturn(priceResponse);
        when(bookingMapper.toEntity(bookingRequest, guest)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);
        doNothing().when(availabilityService).holdRooms(anyLong(), any(), any(), anyInt());

        BookingResponseDto result = bookingService.createBooking(bookingRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(availabilityService).holdRooms(anyLong(), any(), any(), anyInt());
    }

    @Test
    void createBooking_CreatesNewGuest_WhenNotFound() {
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(bookingMapper.toEntity(guestDto)).thenReturn(guest);
        when(guestRepository.save(any(Guest.class))).thenReturn(guest);
        when(bookingRepository.findConflictingBookings(eq(1L), any(), any(), eq(-1L)))
                .thenReturn(new ArrayList<>());
        when(availabilityService.calculatePrice(any())).thenReturn(priceResponse);
        when(bookingMapper.toEntity(bookingRequest, guest)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);
        doNothing().when(availabilityService).holdRooms(anyLong(), any(), any(), anyInt());

        BookingResponseDto result = bookingService.createBooking(bookingRequest);

        assertThat(result).isNotNull();
        verify(guestRepository).save(any(Guest.class));
    }

    @Test
    void createBooking_ThrowsException_WhenCheckInInPast() {
        bookingRequest.setCheckInDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createBooking_ThrowsException_WhenCheckOutBeforeCheckIn() {
        bookingRequest.setCheckOutDate(bookingRequest.getCheckInDate().minusDays(1));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createBooking_ThrowsException_WhenGuestsExceedCapacity() {
        bookingRequest.setNumberOfGuests(10);
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createBooking_ThrowsException_WhenRoomsNotAvailable() {
        Booking conflictingBooking = new Booking();
        conflictingBooking.setNumberOfRooms(20);
        lenient().when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        lenient().when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(guest));
        lenient().when(bookingRepository.findConflictingBookings(eq(1L), any(), any(), eq(-1L)))
                .thenReturn(Arrays.asList(conflictingBooking));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Rooms are not available");

        verify(availabilityService, never()).holdRooms(anyLong(), any(), any(), anyInt());
    }

    @Test
    void getBookingById_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);

        BookingResponseDto result = bookingService.getBookingById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBookingById_ThrowsException_WhenNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(99L))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void getBookingByReference_Success() {
        when(bookingRepository.findByBookingReference("BK12345678")).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);

        BookingResponseDto result = bookingService.getBookingByReference("BK12345678");

        assertThat(result).isNotNull();
        assertThat(result.getBookingReference()).isEqualTo("BK12345678");
    }

    @Test
    void getBookingByReference_ThrowsException_WhenNotFound() {
        when(bookingRepository.findByBookingReference("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingByReference("INVALID"))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void getGuestBookings_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(Arrays.asList(booking));

        when(bookingRepository.findByGuestId(1L, pageable)).thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);

        Page<BookingResponseDto> result = bookingService.getGuestBookings(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getGuestBookingsByStatus_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(Arrays.asList(booking));

        when(bookingRepository.findByGuestIdAndStatus(1L, BookingStatus.PENDING, pageable))
                .thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);

        Page<BookingResponseDto> result = bookingService.getGuestBookingsByStatus(1L, BookingStatus.PENDING, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getHotelBookings_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(Arrays.asList(booking));

        when(bookingRepository.findByHotelId(1L, pageable)).thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);

        Page<BookingResponseDto> result = bookingService.getHotelBookings(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void confirmBooking_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);
        doNothing().when(availabilityService).confirmBooking(anyLong(), any(), any(), anyInt());

        BookingResponseDto result = bookingService.confirmBooking(1L);

        assertThat(result).isNotNull();
        verify(availabilityService).confirmBooking(anyLong(), any(), any(), anyInt());
    }

    @Test
    void confirmBooking_ThrowsException_WhenAlreadyConfirmed() {
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmBooking(1L))
                .isInstanceOf(InvalidBookingStateException.class);

        verify(availabilityService, never()).confirmBooking(anyLong(), any(), any(), anyInt());
    }

    @Test
    void confirmBooking_ThrowsException_WhenAlreadyCancelled() {
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmBooking(1L))
                .isInstanceOf(InvalidBookingStateException.class);

        verify(availabilityService, never()).confirmBooking(anyLong(), any(), any(), anyInt());
    }

    @Test
    void cancelBooking_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);
        doNothing().when(availabilityService).cancelBooking(anyLong(), any(), any(), anyInt());

        BookingResponseDto result = bookingService.cancelBooking(1L, cancelRequest);

        assertThat(result).isNotNull();
        verify(availabilityService).cancelBooking(anyLong(), any(), any(), anyInt());
    }

    @Test
    void cancelBooking_ThrowsException_WhenAlreadyCancelled() {
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L, cancelRequest))
                .isInstanceOf(InvalidBookingStateException.class);

        verify(availabilityService, never()).cancelBooking(anyLong(), any(), any(), anyInt());
    }

    @Test
    void cancelBooking_ThrowsException_WhenAlreadyCompleted() {
        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L, cancelRequest))
                .isInstanceOf(InvalidBookingStateException.class);

        verify(availabilityService, never()).cancelBooking(anyLong(), any(), any(), anyInt());
    }

    @Test
    void updatePaymentStatus_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);

        BookingResponseDto result = bookingService.updatePaymentStatus(1L, new BigDecimal("450.00"));

        assertThat(result).isNotNull();
    }

    @Test
    void updatePaymentStatus_ConfirmsBooking_WhenFullyPaid() {
        booking.setTotalPrice(new BigDecimal("450.00"));
        booking.setPaidAmount(new BigDecimal("0.00"));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);
        doNothing().when(availabilityService).confirmBooking(anyLong(), any(), any(), anyInt());

        BookingResponseDto result = bookingService.updatePaymentStatus(1L, new BigDecimal("450.00"));

        verify(bookingRepository, times(2)).save(any(Booking.class));
        verify(availabilityService).confirmBooking(anyLong(), any(), any(), anyInt());
    }

    @Test
    void checkRoomAvailability_ReturnsTrue_WhenRoomsAvailable() {
        when(bookingRepository.findConflictingBookings(eq(1L), any(), any(), eq(-1L)))
                .thenReturn(new ArrayList<>());
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        boolean result = bookingService.checkRoomAvailability(1L,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(8), 1, null);

        assertThat(result).isTrue();
    }

    @Test
    void checkRoomAvailability_ReturnsFalse_WhenRoomsNotAvailable() {
        Booking conflictingBooking = new Booking();
        conflictingBooking.setNumberOfRooms(20);

        when(bookingRepository.findConflictingBookings(eq(1L), any(), any(), eq(-1L)))
                .thenReturn(Arrays.asList(conflictingBooking));
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        boolean result = bookingService.checkRoomAvailability(1L,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(8), 1, null);

        assertThat(result).isFalse();
    }

    @Test
    void searchBookings_Success() {
        BookingSearchRequestDto searchRequest = new BookingSearchRequestDto();
        searchRequest.setGuestId(1L);
        searchRequest.setHotelId(1L);
        searchRequest.setStatus(BookingStatus.PENDING);
        searchRequest.setBookingReference("BK123");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(Arrays.asList(booking));

        when(bookingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(bookingResponse);

        Page<BookingResponseDto> result = bookingService.searchBookings(searchRequest, pageable);

        assertThat(result).isNotNull();
    }
}
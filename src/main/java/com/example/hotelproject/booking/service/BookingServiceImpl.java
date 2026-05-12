package com.example.hotelproject.booking.service;

import com.example.hotelproject.auth.repository.UserRepository;
import com.example.hotelproject.availability.service.AvailabilityService;
import com.example.hotelproject.email.EmailService;
import com.example.hotelproject.availability.dto.PriceCalculationRequestDto;
import com.example.hotelproject.availability.dto.PriceCalculationResponseDto;
import com.example.hotelproject.booking.dto.*;
import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.booking.entity.Guest;
import com.example.hotelproject.booking.exception.BookingNotFoundException;
import com.example.hotelproject.booking.exception.InvalidBookingStateException;
import com.example.hotelproject.booking.mapper.BookingMapper;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.booking.repository.GuestRepository;
import com.example.hotelproject.catalog.hotel.Hotel;
import com.example.hotelproject.catalog.hotel.HotelRepository;
import com.example.hotelproject.catalog.hotel.HotelNotFoundException;
import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.catalog.room.RoomTypeRepository;
import com.example.hotelproject.catalog.room.RoomTypeNotFoundException;
import com.example.hotelproject.enums.BookingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private static final int CANCELLATION_DAYS_LIMIT = 2;

    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AvailabilityService availabilityService;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final EmailService emailService;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              GuestRepository guestRepository,
                              RoomTypeRepository roomTypeRepository,
                              @Lazy AvailabilityService availabilityService,
                              BookingMapper bookingMapper,
                              UserRepository userRepository,
                              HotelRepository hotelRepository,
                              EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.guestRepository = guestRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.availabilityService = availabilityService;
        this.bookingMapper = bookingMapper;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.emailService = emailService;
    }

    @Override
    public BookingResponseDto createBooking(BookingRequestDto request) {
        log.info("Creating booking for roomTypeId={}, from={} to={}",
                request.getRoomTypeId(), request.getCheckInDate(), request.getCheckOutDate());

        validateBookingRequest(request);

        boolean isAvailable = checkRoomAvailability(
                request.getRoomTypeId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getNumberOfRooms(),
                null
        );

        if (!isAvailable) {
            throw new IllegalStateException("Rooms are not available for the selected dates");
        }

        Guest guest = findOrCreateGuest(request.getGuest());

        PriceCalculationResponseDto priceCalculation = calculatePrice(request);

        Booking booking = bookingMapper.toEntity(request, guest);
        booking.setTotalPrice(priceCalculation.getTotalPrice());

        Booking savedBooking = bookingRepository.save(booking);

        availabilityService.holdRooms(
                request.getRoomTypeId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getNumberOfRooms()
        );

        log.info("Booking created successfully with reference: {}", savedBooking.getBookingReference());
        return bookingMapper.toDto(savedBooking);
    }

    private void validateBookingRequest(BookingRequestDto request) {
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        if (request.getCheckInDate().isAfter(request.getCheckOutDate()) ||
                request.getCheckInDate().equals(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new RoomTypeNotFoundException(request.getRoomTypeId()));

        if (request.getNumberOfGuests() > roomType.getCapacity() * request.getNumberOfRooms()) {
            throw new IllegalArgumentException(
                    String.format("Maximum guests for %d room(s) is %d",
                            request.getNumberOfRooms(),
                            roomType.getCapacity() * request.getNumberOfRooms())
            );
        }
    }

    private Guest findOrCreateGuest(GuestDto guestDto) {
        return guestRepository.findByEmail(guestDto.getEmail())
                .orElseGet(() -> {
                    Guest newGuest = bookingMapper.toEntity(guestDto);
                    return guestRepository.save(newGuest);
                });
    }

    private PriceCalculationResponseDto calculatePrice(BookingRequestDto request) {
        PriceCalculationRequestDto priceRequest = new PriceCalculationRequestDto();
        priceRequest.setRoomTypeId(request.getRoomTypeId());
        priceRequest.setCheckInDate(request.getCheckInDate());
        priceRequest.setCheckOutDate(request.getCheckOutDate());
        priceRequest.setNumberOfRooms(request.getNumberOfRooms());

        return availabilityService.calculatePrice(priceRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long id) {
        log.info("Fetching booking with id: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingByReference(String reference) {
        log.info("Fetching booking with reference: {}", reference);
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with reference: " + reference));
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> getGuestBookings(Long guestId, Pageable pageable) {
        log.info("Fetching bookings for guest: {}", guestId);
        return bookingRepository.findByGuestId(guestId, pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> getGuestBookingsByStatus(Long guestId, BookingStatus status, Pageable pageable) {
        log.info("Fetching {} bookings for guest: {}", status, guestId);
        return bookingRepository.findByGuestIdAndStatus(guestId, status, pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> getHotelBookings(Long hotelId, Pageable pageable) {
        log.info("Fetching bookings for hotel: {}", hotelId);
        return bookingRepository.findByHotelId(hotelId, pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> getHotelBookingsByStatus(Long hotelId, BookingStatus status, Pageable pageable) {
        log.info("Fetching {} bookings for hotel: {}", status, hotelId);
        return bookingRepository.findByHotelIdAndStatus(hotelId, status, pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    public BookingResponseDto confirmBooking(Long bookingId) {
        log.info("Confirming booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.isPending()) {
            throw new InvalidBookingStateException(
                    String.format("Cannot confirm booking in %s state", booking.getStatus())
            );
        }

        booking.confirm();

        availabilityService.confirmBooking(
                booking.getRoomType().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNumberOfRooms()
        );

        Booking confirmedBooking = bookingRepository.save(booking);
        log.info("Booking confirmed with reference: {}", confirmedBooking.getBookingReference());

        emailService.sendBookingConfirmation(confirmedBooking);

        return bookingMapper.toDto(confirmedBooking);
    }

    @Override
    public BookingResponseDto cancelBooking(Long bookingId, CancelBookingRequestDto request) {
        log.info("Cancelling booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        List<BookingStatus> cancellableStatuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        if (!cancellableStatuses.contains(booking.getStatus())) {
            throw new IllegalArgumentException(
                    "Cannot cancel booking with status: " + booking.getStatus()
            );
        }

        validateCancellationPolicy(booking);

        booking.cancel(request.getReason());

        availabilityService.cancelBooking(
                booking.getRoomType().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNumberOfRooms()
        );

        Booking cancelledBooking = bookingRepository.save(booking);
        log.info("Booking cancelled with reference: {}", cancelledBooking.getBookingReference());

        try {
            emailService.sendBookingCancellation(cancelledBooking);
        } catch (Exception e) {
            log.error("Failed to send cancellation email for {}", cancelledBooking.getBookingReference(), e);
        }

        return bookingMapper.toDto(cancelledBooking);
    }

    private void validateCancellationPolicy(Booking booking) {
        if (booking.isConfirmed()) {
            long daysUntilCheckIn = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(), booking.getCheckInDate());

            if (daysUntilCheckIn < CANCELLATION_DAYS_LIMIT) {
                throw new InvalidBookingStateException(
                        String.format("Cancellation is not allowed within %d days of check-in",
                                CANCELLATION_DAYS_LIMIT)
                );
            }
        }
    }

    @Override
    public BookingResponseDto updatePaymentStatus(Long bookingId, BigDecimal amountPaid) {
        log.info("Updating payment for booking id: {}, amount: {}", bookingId, amountPaid);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        BigDecimal newPaidAmount = booking.getPaidAmount().add(amountPaid);
        booking.setPaidAmount(newPaidAmount);

        Booking updatedBooking = bookingRepository.save(booking);

        if (newPaidAmount.compareTo(booking.getTotalPrice()) >= 0 && booking.isPending()) {
            return confirmBooking(bookingId);
        }

        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkRoomAvailability(Long roomTypeId, LocalDate checkIn, LocalDate checkOut,
                                         Integer numberOfRooms, Long currentBookingId) {
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                roomTypeId, checkIn, checkOut,
                currentBookingId != null ? currentBookingId : -1L);

        int totalBookedRooms = conflictingBookings.stream()
                .mapToInt(Booking::getNumberOfRooms)
                .sum();

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RoomTypeNotFoundException(roomTypeId));
        int availableRooms = roomType.getTotalRooms() - totalBookedRooms;

        return availableRooms >= numberOfRooms;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> searchBookings(BookingSearchRequestDto searchRequest, Pageable pageable) {
        log.info("Searching bookings with criteria: {}", searchRequest);

        Specification<Booking> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchRequest.getGuestId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("guest").get("id"), searchRequest.getGuestId()));
            }

            if (searchRequest.getHotelId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("roomType").get("hotel").get("id"), searchRequest.getHotelId()));
            }

            if (searchRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchRequest.getStatus()));
            }

            if (searchRequest.getBookingReference() != null && !searchRequest.getBookingReference().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("bookingReference")),
                        "%" + searchRequest.getBookingReference().toLowerCase() + "%"
                ));
            }

            if (searchRequest.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("checkInDate"), searchRequest.getFromDate()));
            }

            if (searchRequest.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("checkOutDate"), searchRequest.getToDate()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return bookingRepository.findAll(specification, pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    public BookingResponseDto updateBooking(Long id, BookingRequestDto request) {
        log.info("Updating booking with id: {}", id);

        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        if (existingBooking.isConfirmed() || existingBooking.isCompleted()) {
            throw new InvalidBookingStateException(
                    "Cannot update booking in " + existingBooking.getStatus() + " state"
            );
        }

        boolean isAvailable = checkRoomAvailability(
                request.getRoomTypeId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getNumberOfRooms(),
                id
        );

        if (!isAvailable) {
            throw new IllegalStateException("Rooms are not available for the selected dates");
        }

        existingBooking.setRoomType(roomTypeRepository.getReferenceById(request.getRoomTypeId()));
        existingBooking.setCheckInDate(request.getCheckInDate());
        existingBooking.setCheckOutDate(request.getCheckOutDate());
        existingBooking.setNumberOfGuests(request.getNumberOfGuests());
        existingBooking.setNumberOfRooms(request.getNumberOfRooms());
        existingBooking.setSpecialRequests(request.getSpecialRequests());

        PriceCalculationResponseDto priceCalculation = calculatePrice(request);
        existingBooking.setTotalPrice(priceCalculation.getTotalPrice());

        Booking updatedBooking = bookingRepository.save(existingBooking);

        return bookingMapper.toDto(updatedBooking);
    }

    // FIX: 13 Replaced PageRequest.of(0, Integer.MAX_VALUE) with targeted aggregation queries
    // to avoid loading all booking records into memory.
    @Override
    @Transactional(readOnly = true)
    public HotelStatisticsDto getHotelStatistics(Long hotelId) {
        log.info("Getting statistics for hotel: {}", hotelId);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        long totalBookings    = bookingRepository.countAllByHotelId(hotelId);
        long confirmedBookings = bookingRepository.countByHotelIdAndStatus(hotelId, BookingStatus.CONFIRMED);
        long pendingBookings   = bookingRepository.countByHotelIdAndStatus(hotelId, BookingStatus.PENDING);
        long cancelledBookings = bookingRepository.countByHotelIdAndStatus(hotelId, BookingStatus.CANCELLED);
        long completedBookings = bookingRepository.countByHotelIdAndStatus(hotelId, BookingStatus.COMPLETED);

        List<BookingStatus> revenueStatuses = List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);
        BigDecimal totalRevenue = bookingRepository.sumRevenueByHotelId(hotelId, revenueStatuses);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        BigDecimal averageBookingValue = totalBookings > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Long guestsSum = bookingRepository.sumGuestsByHotelId(hotelId);
        long totalGuests = guestsSum != null ? guestsSum : 0L;

        List<RoomType> roomTypes = roomTypeRepository.findByHotelId(hotelId);
        int totalRooms = roomTypes.stream().mapToInt(RoomType::getTotalRooms).sum();

        double occupancyRate = 0.0;
        if (totalRooms > 0) {
            Long roomNightsSum = bookingRepository.sumRoomNightsByHotelId(hotelId);
            long confirmedRoomNights = roomNightsSum != null ? roomNightsSum : 0L;
            long totalRoomNights = (long) totalRooms * 365;
            occupancyRate = totalRoomNights > 0
                    ? (double) confirmedRoomNights / totalRoomNights * 100
                    : 0.0;
        }

        return new HotelStatisticsDto(
                hotelId,
                hotel.getName(),
                totalBookings,
                confirmedBookings,
                pendingBookings,
                cancelledBookings,
                completedBookings,
                totalRevenue,
                averageBookingValue,
                totalGuests,
                occupancyRate
        );
    }

    // FIX: 10 Returns only CONFIRMED bookings with checkIn >= today for the given hotel
    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> getUpcomingHotelBookings(Long hotelId, Pageable pageable) {
        log.info("Fetching upcoming bookings for hotel: {}", hotelId);
        return bookingRepository.findUpcomingByHotelId(hotelId, LocalDate.now(), pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    public BookingResponseDto completeBooking(Long bookingId) {
        log.info("Completing booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.isConfirmed()) {
            throw new InvalidBookingStateException(
                    "Cannot complete booking in " + booking.getStatus() + " state. Must be CONFIRMED first.");
        }

        booking.complete();
        Booking completed = bookingRepository.save(booking);
        log.info("Booking completed: {}", completed.getBookingReference());
        return bookingMapper.toDto(completed);
    }

    @Override
    public BookingResponseDto markNoShow(Long bookingId) {
        log.info("Marking booking as NO_SHOW with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.isConfirmed()) {
            throw new InvalidBookingStateException(
                    "Cannot mark no-show for booking in " + booking.getStatus() + " state. Must be CONFIRMED first.");
        }

        booking.setStatus(BookingStatus.NO_SHOW);
        Booking updated = bookingRepository.save(booking);
        log.info("Booking marked as NO_SHOW: {}", updated.getBookingReference());
        return bookingMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> getMyBookings(String usernameOrEmail, Pageable pageable) {
        log.info("Fetching bookings for principal: {}", usernameOrEmail);
        String email = userRepository.findByUsername(usernameOrEmail)
                .map(u -> u.getEmail())
                .orElse(usernameOrEmail);
        return bookingRepository.findByGuestEmail(email, pageable)
                .map(bookingMapper::toDto);
    }
}
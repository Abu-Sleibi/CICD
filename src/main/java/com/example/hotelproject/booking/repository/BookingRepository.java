package com.example.hotelproject.booking.repository;

import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    Optional<Booking> findByBookingReference(String bookingReference);

    Page<Booking> findByGuestId(Long guestId, Pageable pageable);

    Page<Booking> findByGuestIdAndStatus(Long guestId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.roomType.hotel.id = :hotelId")
    Page<Booking> findByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.roomType.hotel.id = :hotelId AND b.status = :status")
    Page<Booking> findByHotelIdAndStatus(
            @Param("hotelId") Long hotelId,
            @Param("status") BookingStatus status,
            Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.checkInDate >= :startDate AND b.checkOutDate <= :endDate")
    Page<Booking> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.roomType.id = :roomTypeId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate " +
            "AND b.id != :currentBookingId")
    List<Booking> findConflictingBookings(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("currentBookingId") Long currentBookingId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.roomType.id = :roomTypeId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate " +
            "AND b.id != :currentBookingId")
    boolean existsConflictingBooking(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("currentBookingId") Long currentBookingId);

    @Query("SELECT b FROM Booking b WHERE b.checkOutDate < :date AND b.status = 'CONFIRMED'")
    List<Booking> findCompletedBookings(@Param("date") LocalDate date);

    long countByGuestIdAndStatus(Long guestId, BookingStatus status);

    // FIX: 13 Proper aggregation queries to replace PageRequest.of(0, Integer.MAX_VALUE) in statistics

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.roomType.hotel.id = :hotelId")
    long countAllByHotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.roomType.hotel.id = :hotelId AND b.status = :status")
    long countByHotelIdAndStatus(@Param("hotelId") Long hotelId, @Param("status") BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.roomType.hotel.id = :hotelId AND b.status IN :statuses")
    BigDecimal sumRevenueByHotelId(@Param("hotelId") Long hotelId, @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT COALESCE(SUM(b.numberOfGuests), 0) FROM Booking b WHERE b.roomType.hotel.id = :hotelId")
    Long sumGuestsByHotelId(@Param("hotelId") Long hotelId);

    @Query(value = "SELECT COALESCE(SUM(b.number_of_rooms * DATEDIFF(b.check_out_date, b.check_in_date)), 0) " +
            "FROM bookings b " +
            "INNER JOIN room_types rt ON b.room_type_id = rt.id " +
            "WHERE rt.hotel_id = :hotelId AND b.status IN ('CONFIRMED', 'COMPLETED')",
            nativeQuery = true)
    Long sumRoomNightsByHotelId(@Param("hotelId") Long hotelId);

    // FIX: 9 Sum of booked rooms for a room type in a date range (used by hotel search date filter)
    @Query("SELECT COALESCE(SUM(b.numberOfRooms), 0) FROM Booking b " +
            "WHERE b.roomType.id = :roomTypeId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.checkInDate < :checkOut AND b.checkOutDate > :checkIn")
    Integer sumBookedRoomsForRoomType(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    // FIX: 10 Upcoming bookings for a hotel (checkIn >= today and status = CONFIRMED)
    @Query("SELECT b FROM Booking b WHERE b.roomType.hotel.id = :hotelId " +
            "AND b.checkInDate >= :today AND b.status = 'CONFIRMED'")
    Page<Booking> findUpcomingByHotelId(
            @Param("hotelId") Long hotelId,
            @Param("today") LocalDate today,
            Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.guest.email = :email")
    Page<Booking> findByGuestEmail(@Param("email") String email, Pageable pageable);

    boolean existsByRoomTypeIdAndStatusIn(Long roomTypeId, java.util.Collection<BookingStatus> statuses);

    long countByStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status IN :statuses")
    BigDecimal sumTotalRevenue(@Param("statuses") List<BookingStatus> statuses);
}
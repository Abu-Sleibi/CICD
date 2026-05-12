package com.example.hotelproject.admin.service;

import com.example.hotelproject.admin.dto.SystemStatisticsDto;
import com.example.hotelproject.auth.repository.UserRepository;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.booking.repository.GuestRepository;
import com.example.hotelproject.catalog.hotel.HotelRepository;
import com.example.hotelproject.catalog.room.RoomTypeRepository;
import com.example.hotelproject.enums.BookingStatus;
import com.example.hotelproject.notification.repository.NotificationRepository;
import com.example.hotelproject.payment.repository.PaymentRepository;
import com.example.hotelproject.review.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;

    public AdminServiceImpl(HotelRepository hotelRepository,
                             RoomTypeRepository roomTypeRepository,
                             UserRepository userRepository,
                             GuestRepository guestRepository,
                             BookingRepository bookingRepository,
                             PaymentRepository paymentRepository,
                             NotificationRepository notificationRepository,
                             ReviewRepository reviewRepository) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.userRepository = userRepository;
        this.guestRepository = guestRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public SystemStatisticsDto getSystemStatistics() {
        log.info("Generating system-wide statistics");

        SystemStatisticsDto stats = new SystemStatisticsDto();

        stats.setTotalHotels(hotelRepository.count());
        stats.setActiveHotels(hotelRepository.countByActiveTrue());
        stats.setTotalRoomTypes(roomTypeRepository.count());
        stats.setTotalUsers(userRepository.count());
        stats.setTotalGuests(guestRepository.count());

        long totalBookings = bookingRepository.count();
        stats.setTotalBookings(totalBookings);
        stats.setPendingBookings(bookingRepository.countByStatus(BookingStatus.PENDING));
        stats.setConfirmedBookings(bookingRepository.countByStatus(BookingStatus.CONFIRMED));
        stats.setCancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED));
        stats.setCompletedBookings(bookingRepository.countByStatus(BookingStatus.COMPLETED));
        stats.setNoShowBookings(bookingRepository.countByStatus(BookingStatus.NO_SHOW));

        List<BookingStatus> revenueStatuses = List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);
        BigDecimal totalRevenue = bookingRepository.sumTotalRevenue(revenueStatuses);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        stats.setTotalRevenue(totalRevenue);

        BigDecimal avgValue = totalBookings > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        stats.setAverageBookingValue(avgValue);

        stats.setTotalPayments(paymentRepository.count());
        stats.setTotalRefunded(BigDecimal.ZERO);

        stats.setTotalNotifications(notificationRepository.count());
        stats.setTotalReviews(reviewRepository.count());

        Double avgRating = reviewRepository.findOverallAverageRating();
        stats.setOverallAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

        return stats;
    }
}

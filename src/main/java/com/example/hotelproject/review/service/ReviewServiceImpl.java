package com.example.hotelproject.review.service;

import com.example.hotelproject.auth.repository.UserRepository;
import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.booking.exception.BookingNotFoundException;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.enums.BookingStatus;
import com.example.hotelproject.review.dto.ReviewRequestDto;
import com.example.hotelproject.review.dto.ReviewResponseDto;
import com.example.hotelproject.review.entity.Review;
import com.example.hotelproject.review.exception.ReviewNotFoundException;
import com.example.hotelproject.review.mapper.ReviewMapper;
import com.example.hotelproject.review.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                              BookingRepository bookingRepository,
                              ReviewMapper reviewMapper,
                              UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.reviewMapper = reviewMapper;
        this.userRepository = userRepository;
    }

    private String resolveEmail(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .map(u -> u.getEmail())
                .orElse(usernameOrEmail);
    }

    @Override
    public ReviewResponseDto createReview(ReviewRequestDto request, String principal) {
        String guestEmail = resolveEmail(principal);
        log.info("Creating review for bookingId={} by guest={}", request.getBookingId(), guestEmail);

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(request.getBookingId()));

        if (!booking.getGuest().getEmail().equalsIgnoreCase(guestEmail)) {
            throw new IllegalArgumentException("You can only review your own bookings");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Reviews can only be submitted for completed bookings");
        }

        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new IllegalStateException("A review has already been submitted for this booking");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setGuest(booking.getGuest());
        review.setHotel(booking.getRoomType().getHotel());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        log.info("Review created with id={} for hotel={}", saved.getId(), saved.getHotel().getName());
        return reviewMapper.toDto(saved);
    }

    @Override
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto request, String principal) {
        String guestEmail = resolveEmail(principal);
        log.info("Updating review id={} by guest={}", reviewId, guestEmail);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!review.getGuest().getEmail().equalsIgnoreCase(guestEmail)) {
            throw new IllegalArgumentException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(Long reviewId, String principal) {
        String guestEmail = resolveEmail(principal);
        log.info("Deleting review id={} by guest={}", reviewId, guestEmail);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!review.getGuest().getEmail().equalsIgnoreCase(guestEmail)) {
            throw new IllegalArgumentException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
        return reviewMapper.toDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewByBookingId(Long bookingId) {
        Review review = reviewRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ReviewNotFoundException("No review found for bookingId: " + bookingId));
        return reviewMapper.toDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsByHotel(Long hotelId, Pageable pageable) {
        return reviewRepository.findByHotelId(hotelId, pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsByGuest(Long guestId, Pageable pageable) {
        return reviewRepository.findByGuestId(guestId, pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingForHotel(Long hotelId) {
        Double avg = reviewRepository.findAverageRatingByHotelId(hotelId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public long getReviewCountForHotel(Long hotelId) {
        return reviewRepository.countByHotelId(hotelId);
    }
}

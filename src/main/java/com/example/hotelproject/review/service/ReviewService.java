package com.example.hotelproject.review.service;

import com.example.hotelproject.review.dto.ReviewRequestDto;
import com.example.hotelproject.review.dto.ReviewResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponseDto createReview(ReviewRequestDto request, String guestEmail);

    ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto request, String guestEmail);

    void deleteReview(Long reviewId, String guestEmail);

    ReviewResponseDto getReviewById(Long id);

    ReviewResponseDto getReviewByBookingId(Long bookingId);

    Page<ReviewResponseDto> getReviewsByHotel(Long hotelId, Pageable pageable);

    Page<ReviewResponseDto> getReviewsByGuest(Long guestId, Pageable pageable);

    Double getAverageRatingForHotel(Long hotelId);

    long getReviewCountForHotel(Long hotelId);
}

package com.example.hotelproject.review.mapper;

import com.example.hotelproject.review.dto.ReviewResponseDto;
import com.example.hotelproject.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponseDto toDto(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getHotel().getId(),
                review.getHotel().getName(),
                review.getGuest().getId(),
                review.getGuest().getFullName(),
                review.getBooking().getId(),
                review.getBooking().getBookingReference(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}

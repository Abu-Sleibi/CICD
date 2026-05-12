package com.example.hotelproject.review.controller;

import com.example.hotelproject.auth.security.CurrentUser;
import com.example.hotelproject.review.dto.ReviewRequestDto;
import com.example.hotelproject.review.dto.ReviewResponseDto;
import com.example.hotelproject.review.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "08. Reviews", description = "Guest hotel reviews and ratings (completed bookings only)")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewRequestDto request,
            @CurrentUser UserDetails userDetails,
            UriComponentsBuilder uriBuilder) {

        ReviewResponseDto response = reviewService.createReview(request, userDetails.getUsername());

        URI location = uriBuilder
                .path("/api/v1/reviews/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto request,
            @CurrentUser UserDetails userDetails) {

        ReviewResponseDto response = reviewService.updateReview(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @CurrentUser UserDetails userDetails) {

        reviewService.deleteReview(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ReviewResponseDto> getReviewByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(reviewService.getReviewByBookingId(bookingId));
    }

    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByHotel(
            @PathVariable Long hotelId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(reviewService.getReviewsByHotel(hotelId, pageable));
    }

    @GetMapping("/guests/{guestId}")
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurity.isCurrentUserGuest(#guestId)")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByGuest(
            @PathVariable Long guestId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(reviewService.getReviewsByGuest(guestId, pageable));
    }

    @GetMapping("/hotels/{hotelId}/rating")
    public ResponseEntity<HotelRatingSummary> getHotelRating(@PathVariable Long hotelId) {
        Double avg = reviewService.getAverageRatingForHotel(hotelId);
        long count = reviewService.getReviewCountForHotel(hotelId);
        return ResponseEntity.ok(new HotelRatingSummary(hotelId, avg, count));
    }

    record HotelRatingSummary(Long hotelId, Double averageRating, Long reviewCount) {}
}

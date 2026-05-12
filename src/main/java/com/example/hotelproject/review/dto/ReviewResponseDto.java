package com.example.hotelproject.review.dto;

import java.time.LocalDateTime;

public class ReviewResponseDto {

    private Long id;
    private Long hotelId;
    private String hotelName;
    private Long guestId;
    private String guestName;
    private Long bookingId;
    private String bookingReference;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReviewResponseDto() {}

    public ReviewResponseDto(Long id, Long hotelId, String hotelName, Long guestId, String guestName,
                              Long bookingId, String bookingReference, Integer rating, String comment,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.guestId = guestId;
        this.guestName = guestName;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

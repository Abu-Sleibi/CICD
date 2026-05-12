package com.example.hotelproject.catalog.hotel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    Optional<Hotel> findByIdAndActiveTrue(Long id);

    Page<Hotel> findAllByActiveTrue(Pageable pageable);

    List<Hotel> findByCityAndActiveTrue(String city);

    List<Hotel> findByCountryAndActiveTrue(String country);

    List<Hotel> findByStarRatingAndActiveTrue(Integer starRating);

    @Query("SELECT h FROM Hotel h WHERE h.active = true AND " +
            "(:city IS NULL OR h.city = :city) AND " +
            "(:country IS NULL OR h.country = :country) AND " +
            "(:minRating IS NULL OR h.starRating >= :minRating)")
    Page<Hotel> searchHotels(
            @Param("city") String city,
            @Param("country") String country,
            @Param("minRating") Integer minRating,
            Pageable pageable);

    @Query("SELECT DISTINCT h FROM Hotel h JOIN h.roomTypes rt WHERE h.active = true AND rt.active = true AND " +
            ":amenity MEMBER OF rt.amenities AND " +
            "(:city IS NULL OR h.city = :city) AND " +
            "(:country IS NULL OR h.country = :country) AND " +
            "(:minRating IS NULL OR h.starRating >= :minRating)")
    List<Hotel> searchHotelsWithAmenity(
            @Param("amenity") String amenity,
            @Param("city") String city,
            @Param("country") String country,
            @Param("minRating") Integer minRating);

    boolean existsByNameAndCity(String name, String city);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    long countByActiveTrue();
}
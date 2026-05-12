package com.example.hotelproject.catalog.hotel;

import com.example.hotelproject.PageResponseDto;
import com.example.hotelproject.catalog.room.RoomTypeRequestDto;
import com.example.hotelproject.catalog.room.RoomTypeResponseDto;
import com.example.hotelproject.auth.security.CurrentUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hotels")
@Tag(name = "02. Hotel Catalog", description = "Hotel management CRUD operations")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    // ========== HOTEL CRUD ==========

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelResponseDto> createHotel(
            @Valid @RequestBody HotelRequestDto hotelRequest,
            UriComponentsBuilder uriBuilder) {
        HotelResponseDto response = hotelService.createHotel(hotelRequest);

        URI location = uriBuilder
                .path("/api/v1/hotels/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<List<HotelResponseDto>> getManagerHotels(
            @CurrentUser UserDetails userDetails) {
        List<HotelResponseDto> hotels = hotelService.getManagerHotels(userDetails.getUsername());
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponseDto> getHotelById(@PathVariable Long id) {
        HotelResponseDto response = hotelService.getHotelById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<HotelResponseDto>> getAllHotels(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<HotelResponseDto> hotelsPage = hotelService.getAllHotels(pageable);
        PageResponseDto<HotelResponseDto> response = new PageResponseDto<>(hotelsPage);
        return ResponseEntity.ok(response);
    }

    // FIX: 7 HOTEL_MANAGER can now update their own hotel (was ADMIN only)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<HotelResponseDto> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelRequestDto hotelRequest) {
        HotelResponseDto response = hotelService.updateHotel(id, hotelRequest);
        return ResponseEntity.ok(response);
    }

    // FIX: 7 HOTEL_MANAGER can now delete their own hotel (was ADMIN only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateHotel(@PathVariable Long id) {
        hotelService.activateHotel(id);
        return ResponseEntity.ok().build();
    }

    // ========== ROOM TYPE CRUD ==========

    // FIX: 1 Room type create was unprotected; any guest could create room types
    @PostMapping("/room-types")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<RoomTypeResponseDto> createRoomType(
            @Valid @RequestBody RoomTypeRequestDto roomTypeRequest,
            UriComponentsBuilder uriBuilder) {
        RoomTypeResponseDto response = hotelService.createRoomType(roomTypeRequest);

        URI location = uriBuilder
                .path("/api/v1/hotels/room-types/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/room-types/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoomTypeResponseDto> getRoomTypeById(@PathVariable Long id) {
        RoomTypeResponseDto response = hotelService.getRoomTypeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hotelId}/room-types")
    public ResponseEntity<List<RoomTypeResponseDto>> getRoomTypesByHotelId(@PathVariable Long hotelId) {
        List<RoomTypeResponseDto> roomTypes = hotelService.getRoomTypesByHotelId(hotelId);
        return ResponseEntity.ok(roomTypes);
    }

    // FIX: 1 Room type update was unprotected; any guest could update room types
    @PutMapping("/room-types/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<RoomTypeResponseDto> updateRoomType(
            @PathVariable Long id,
            @Valid @RequestBody RoomTypeRequestDto roomTypeRequest) {
        RoomTypeResponseDto response = hotelService.updateRoomType(id, roomTypeRequest);
        return ResponseEntity.ok(response);
    }

    // FIX: 1 Room type delete was unprotected; any guest could delete room types
    @DeleteMapping("/room-types/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long id) {
        hotelService.deleteRoomType(id);
        return ResponseEntity.noContent().build();
    }

    // ========== SEARCH & FILTER ==========

    // FIX: 9 Unified hotel search with date range, guests, price, and amenity filters
    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<HotelResponseDto>> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> amenities,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<HotelResponseDto> hotelsPage = hotelService.searchHotels(
                city, country, minRating, checkIn, checkOut, guests, minPrice, maxPrice, amenities, pageable);
        return ResponseEntity.ok(new PageResponseDto<>(hotelsPage));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<HotelResponseDto>> getHotelsByCity(@PathVariable String city) {
        List<HotelResponseDto> hotels = hotelService.getHotelsByCity(city);
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<HotelResponseDto>> getHotelsByCountry(@PathVariable String country) {
        List<HotelResponseDto> hotels = hotelService.getHotelsByCountry(country);
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/rating/{starRating}")
    public ResponseEntity<List<HotelResponseDto>> getHotelsByRating(@PathVariable Integer starRating) {
        List<HotelResponseDto> hotels = hotelService.getHotelsByRating(starRating);
        return ResponseEntity.ok(hotels);
    }
}
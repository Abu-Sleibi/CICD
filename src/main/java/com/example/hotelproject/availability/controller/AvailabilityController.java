package com.example.hotelproject.availability.controller;

import com.example.hotelproject.availability.dto.*;
import com.example.hotelproject.availability.service.AvailabilityService;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/availability")
@Tag(name = "04. Availability & Pricing", description = "Check room availability and calculate prices")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    // @Operation(summary = "Check room availability",
    //            description = "Check available rooms for a hotel or specific room type within date range")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Availability check completed"),
    //     @ApiResponse(responseCode = "400", description = "Invalid date range or parameters"),
    //     @ApiResponse(responseCode = "404", description = "Hotel or room type not found")
    // })
    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AvailabilityResponseDto> checkAvailability(
            @Valid @RequestBody AvailabilityRequestDto request) {
        AvailabilityResponseDto response = availabilityService.checkAvailability(request);
        return ResponseEntity.ok(response);
    }

    // @Operation(summary = "Calculate price",
    //            description = "Calculate total price for a stay including all pricing rules")
    @PostMapping("/calculate-price")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PriceCalculationResponseDto> calculatePrice(
            @Valid @RequestBody PriceCalculationRequestDto request) {
        PriceCalculationResponseDto response = availabilityService.calculatePrice(request);
        return ResponseEntity.ok(response);
    }

    // @Operation(summary = "Hold rooms",
    //            description = "Temporarily hold rooms for a pending booking (15-minute hold)")
    @PostMapping("/hold")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> holdRooms(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(defaultValue = "1") int numberOfRooms) {

        availabilityService.holdRooms(roomTypeId, checkInDate, checkOutDate, numberOfRooms);
        return ResponseEntity.ok().build();
    }

    // FIX: 3 Added role guard to state-change endpoint
    @PostMapping("/release-hold")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<Void> releaseHold(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam int numberOfRooms) {

        availabilityService.releaseHold(roomTypeId, checkInDate, checkOutDate, numberOfRooms);
        return ResponseEntity.ok().build();
    }

    // FIX: 3 Added role guard to state-change endpoint
    @PostMapping("/confirm-booking")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<Void> confirmBooking(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam int numberOfRooms) {

        availabilityService.confirmBooking(roomTypeId, checkInDate, checkOutDate, numberOfRooms);
        return ResponseEntity.ok().build();
    }

    // FIX: 3 Added role guard to state-change endpoint
    @PostMapping("/cancel-booking")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<Void> cancelBooking(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam int numberOfRooms) {

        availabilityService.cancelBooking(roomTypeId, checkInDate, checkOutDate, numberOfRooms);
        return ResponseEntity.ok().build();
    }

    // FIX: 3 Added role guard to state-change endpoint
    @PostMapping("/initialize")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<Void> initializeAvailability(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        availabilityService.initializeRoomAvailability(roomTypeId, startDate, endDate);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // @Operation(summary = "Block dates",
    //            description = "Block rooms for maintenance or other purposes (admin only)")
    @PostMapping("/block-dates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_MANAGER')")
    public ResponseEntity<BlockedDateResponseDto> blockDates(
            @Valid @RequestBody BlockDateRequestDto request) {
        BlockedDateResponseDto response = availabilityService.blockDates(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // FIX: 3 Added role guard to state-change endpoint
    @DeleteMapping("/block-dates/{blockedDateId}")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<Void> unblockDates(@PathVariable Long blockedDateId) {
        availabilityService.unblockDates(blockedDateId);
        return ResponseEntity.noContent().build();
    }

    // @Operation(summary = "Get active blocks",
    //            description = "Get all active blocks for a hotel within a date range")
    @GetMapping("/hotels/{hotelId}/blocks")
    public ResponseEntity<List<BlockedDateResponseDto>> getActiveBlocks(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<BlockedDateResponseDto> blocks =
                availabilityService.getActiveBlocksForHotel(hotelId, startDate, endDate);
        return ResponseEntity.ok(blocks);
    }

    // @Operation(summary = "Create pricing rule",
    //            description = "Create a new pricing rule (admin only)")
    @PostMapping("/pricing-rules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PricingRuleResponseDto> createPricingRule(
            @Valid @RequestBody PricingRuleRequestDto request) {
        PricingRuleResponseDto response = availabilityService.createPricingRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // FIX: 3 Added role guard to state-change endpoint
    @PutMapping("/pricing-rules/{ruleId}")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<PricingRuleResponseDto> updatePricingRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody PricingRuleRequestDto request) {
        PricingRuleResponseDto response = availabilityService.updatePricingRule(ruleId, request);
        return ResponseEntity.ok(response);
    }

    // FIX: 3 Added role guard to state-change endpoint
    @DeleteMapping("/pricing-rules/{ruleId}")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<Void> deletePricingRule(@PathVariable Long ruleId) {
        availabilityService.deletePricingRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    // @Operation(summary = "Get hotel pricing rules",
    //            description = "Get all pricing rules for a hotel")
    @GetMapping("/hotels/{hotelId}/pricing-rules")
    public ResponseEntity<List<PricingRuleResponseDto>> getHotelPricingRules(
            @PathVariable Long hotelId) {
        List<PricingRuleResponseDto> rules =
                availabilityService.getPricingRulesForHotel(hotelId);
        return ResponseEntity.ok(rules);
    }

    // @Operation(summary = "Get room type pricing rules",
    //            description = "Get all pricing rules for a specific room type")
    @GetMapping("/room-types/{roomTypeId}/pricing-rules")
    public ResponseEntity<List<PricingRuleResponseDto>> getRoomTypePricingRules(
            @PathVariable Long roomTypeId) {
        List<PricingRuleResponseDto> rules =
                availabilityService.getPricingRulesForRoomType(roomTypeId);
        return ResponseEntity.ok(rules);
    }

    // FIX: 5 Removed orphaned @GetMapping("/reports/hotels/{hotelId}") whose method body was
    // commented out; the annotation was incorrectly applied to bulkUpdateAvailability below.
    // Endpoint removed until properly implemented.

    // FIX: 3 Added role guard to state-change endpoint
    @PatchMapping("/bulk-update")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_MANAGER')")
    public ResponseEntity<Void> bulkUpdateAvailability(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer priceAdjustmentPercentage,
            @RequestParam(required = false) Integer roomsToAdd) {

        availabilityService.bulkUpdateAvailability(
                hotelId, startDate, endDate, priceAdjustmentPercentage, roomsToAdd);
        return ResponseEntity.ok().build();
    }
}
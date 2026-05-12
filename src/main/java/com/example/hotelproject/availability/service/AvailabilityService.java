package com.example.hotelproject.availability.service;

import com.example.hotelproject.availability.dto.*;
import com.example.hotelproject.availability.entity.RoomAvailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityService {

    // Availability check
    AvailabilityResponseDto checkAvailability(AvailabilityRequestDto request);

    // Price calculation
    PriceCalculationResponseDto calculatePrice(PriceCalculationRequestDto request);

    // Room availability management
    void initializeRoomAvailability(Long roomTypeId, LocalDate startDate, LocalDate endDate);

    void holdRooms(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, int numberOfRooms);

    void releaseHold(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, int numberOfRooms);

    void confirmBooking(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, int numberOfRooms);

    void cancelBooking(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, int numberOfRooms);

    // Blocked dates management
    BlockedDateResponseDto blockDates(BlockDateRequestDto request);

    void unblockDates(Long blockedDateId);

    List<BlockedDateResponseDto> getActiveBlocksForHotel(Long hotelId, LocalDate startDate, LocalDate endDate);

    // Pricing rules management
    PricingRuleResponseDto createPricingRule(PricingRuleRequestDto request);

    PricingRuleResponseDto updatePricingRule(Long ruleId, PricingRuleRequestDto request);

    void deletePricingRule(Long ruleId);

    List<PricingRuleResponseDto> getPricingRulesForHotel(Long hotelId);

    List<PricingRuleResponseDto> getPricingRulesForRoomType(Long roomTypeId);

    // Bulk operations
    void bulkUpdateAvailability(Long hotelId, LocalDate startDate, LocalDate endDate,
                                Integer priceAdjustmentPercentage, Integer roomsToAdd);

    // Reports and analytics
    Page<RoomAvailability> getAvailabilityReport(Long hotelId, LocalDate startDate,
                                                 LocalDate endDate, Pageable pageable);
}
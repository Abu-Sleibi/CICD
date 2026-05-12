package com.example.hotelproject.availability.mapper;

import com.example.hotelproject.availability.dto.*;
import com.example.hotelproject.availability.entity.BlockedDate;
import com.example.hotelproject.catalog.hotel.Hotel;
import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.enums.DayType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AvailabilityMapper {

    public AvailabilityResponseDto toAvailabilityResponseDto(
            Hotel hotel,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer guests,
            List<RoomTypeAvailabilityInfo> roomTypesInfo) {

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        boolean anyAvailable = roomTypesInfo.stream().anyMatch(info -> info.getAvailableRooms() > 0);

        List<AvailabilityResponseDto.RoomAvailabilityDetail> details = roomTypesInfo.stream()
                .map(this::toRoomAvailabilityDetail)
                .collect(Collectors.toList());

        return new AvailabilityResponseDto(
                hotel.getId(),
                hotel.getName(),
                checkInDate,
                checkOutDate,
                nights,
                guests,
                anyAvailable,
                details
        );
    }

    private AvailabilityResponseDto.RoomAvailabilityDetail toRoomAvailabilityDetail(
            RoomTypeAvailabilityInfo info) {

        List<AvailabilityResponseDto.DailyAvailability> dailyBreakdown = info.getDailyAvailabilities()
                .stream()
                .map(day -> new AvailabilityResponseDto.DailyAvailability(
                        day.getDate(),
                        day.isAvailable(),
                        day.getAvailableRooms(),
                        day.getPrice()))
                .collect(Collectors.toList());

        BigDecimal avgPricePerNight = info.getTotalPrice()
                .divide(BigDecimal.valueOf(info.getNights()), 2, RoundingMode.HALF_UP);

        return new AvailabilityResponseDto.RoomAvailabilityDetail(
                info.getRoomTypeId(),
                info.getRoomTypeName(),
                info.getCapacity(),
                info.getAvailableRooms(),
                avgPricePerNight,
                info.getTotalPrice(),
                dailyBreakdown
        );
    }

    public PriceCalculationResponseDto toPriceCalculationResponseDto(
            RoomType roomType,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer numberOfRooms,
            BigDecimal basePricePerNight,
            BigDecimal finalPricePerNight,
            List<PriceBreakdownInfo> breakdownInfos,
            List<AppliedRuleInfo> ruleInfos) {

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        BigDecimal subtotal = finalPricePerNight.multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(numberOfRooms));

        BigDecimal discounts = BigDecimal.ZERO;
        BigDecimal taxes = subtotal.multiply(new BigDecimal("0.10"));
        BigDecimal totalPrice = subtotal.add(taxes).subtract(discounts);

        List<PriceCalculationResponseDto.PriceBreakdown> dailyBreakdown = breakdownInfos.stream()
                .map(info -> new PriceCalculationResponseDto.PriceBreakdown(
                        info.getDate(),
                        info.getBasePrice(),
                        info.getAdjustedPrice(),
                        info.getPricePerRoom(),
                        info.getTotalForDay()))
                .collect(Collectors.toList());

        List<PriceCalculationResponseDto.AppliedRule> appliedRules = ruleInfos.stream()
                .map(info -> new PriceCalculationResponseDto.AppliedRule(
                        info.getRuleName(),
                        info.getDescription(),
                        info.getAdjustment()))
                .collect(Collectors.toList());

        return new PriceCalculationResponseDto(
                roomType.getId(),
                roomType.getName(),
                roomType.getHotel().getId(),
                roomType.getHotel().getName(),
                checkInDate,
                checkOutDate,
                nights,
                numberOfRooms,
                basePricePerNight,
                finalPricePerNight,
                subtotal,
                discounts,
                taxes,
                totalPrice,
                dailyBreakdown,
                appliedRules
        );
    }

    public DayType getDayType(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
            return DayType.WEEKEND;
        }
        return DayType.WEEKDAY;
    }

    public BlockedDate toEntity(BlockDateRequestDto dto, Hotel hotel, RoomType roomType) {
        BlockedDate blockedDate = new BlockedDate();
        blockedDate.setHotel(hotel);
        blockedDate.setRoomType(roomType);
        blockedDate.setStartDate(dto.getStartDate());
        blockedDate.setEndDate(dto.getEndDate());
        blockedDate.setReason(dto.getReason());
        blockedDate.setBlockedRoomsCount(dto.getRoomsToBlock());
        blockedDate.setActive(true);
        return blockedDate;
    }

    public static class RoomTypeAvailabilityInfo {
        private final Long roomTypeId;
        private final String roomTypeName;
        private final Integer capacity;
        private final Integer availableRooms;
        private final BigDecimal totalPrice;
        private final long nights;
        private final List<DailyAvailabilityInfo> dailyAvailabilities;

        public RoomTypeAvailabilityInfo(Long roomTypeId, String roomTypeName,
                                        Integer capacity, Integer availableRooms,
                                        BigDecimal totalPrice, long nights,
                                        List<DailyAvailabilityInfo> dailyAvailabilities) {
            this.roomTypeId = roomTypeId;
            this.roomTypeName = roomTypeName;
            this.capacity = capacity;
            this.availableRooms = availableRooms;
            this.totalPrice = totalPrice;
            this.nights = nights;
            this.dailyAvailabilities = dailyAvailabilities;
        }

        public Long getRoomTypeId() { return roomTypeId; }
        public String getRoomTypeName() { return roomTypeName; }
        public Integer getCapacity() { return capacity; }
        public Integer getAvailableRooms() { return availableRooms; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public long getNights() { return nights; }
        public List<DailyAvailabilityInfo> getDailyAvailabilities() { return dailyAvailabilities; }
    }

    public static class DailyAvailabilityInfo {
        private final LocalDate date;
        private final boolean available;
        private final Integer availableRooms;
        private final BigDecimal price;

        public DailyAvailabilityInfo(LocalDate date, boolean available,
                                     Integer availableRooms, BigDecimal price) {
            this.date = date;
            this.available = available;
            this.availableRooms = availableRooms;
            this.price = price;
        }

        public LocalDate getDate() { return date; }
        public boolean isAvailable() { return available; }
        public Integer getAvailableRooms() { return availableRooms; }
        public BigDecimal getPrice() { return price; }
    }

    public static class PriceBreakdownInfo {
        private final LocalDate date;
        private final BigDecimal basePrice;
        private final BigDecimal adjustedPrice;
        private final BigDecimal pricePerRoom;
        private final BigDecimal totalForDay;

        public PriceBreakdownInfo(LocalDate date, BigDecimal basePrice,
                                  BigDecimal adjustedPrice, BigDecimal pricePerRoom,
                                  BigDecimal totalForDay) {
            this.date = date;
            this.basePrice = basePrice;
            this.adjustedPrice = adjustedPrice;
            this.pricePerRoom = pricePerRoom;
            this.totalForDay = totalForDay;
        }

        public LocalDate getDate() { return date; }
        public BigDecimal getBasePrice() { return basePrice; }
        public BigDecimal getAdjustedPrice() { return adjustedPrice; }
        public BigDecimal getPricePerRoom() { return pricePerRoom; }
        public BigDecimal getTotalForDay() { return totalForDay; }
    }

    public static class AppliedRuleInfo {
        private final String ruleName;
        private final String description;
        private final BigDecimal adjustment;

        public AppliedRuleInfo(String ruleName, String description, BigDecimal adjustment) {
            this.ruleName = ruleName;
            this.description = description;
            this.adjustment = adjustment;
        }

        public String getRuleName() { return ruleName; }
        public String getDescription() { return description; }
        public BigDecimal getAdjustment() { return adjustment; }
    }
}
package com.example.hotelproject.availability.service;

import com.example.hotelproject.booking.entity.Booking;
import com.example.hotelproject.booking.repository.BookingRepository;
import com.example.hotelproject.catalog.hotel.Hotel;
import com.example.hotelproject.catalog.hotel.HotelRepository;
import com.example.hotelproject.catalog.hotel.HotelNotFoundException;
import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.catalog.room.RoomTypeNotFoundException;
import com.example.hotelproject.catalog.room.RoomTypeRepository;
import com.example.hotelproject.availability.dto.*;
import com.example.hotelproject.availability.entity.BlockedDate;
import com.example.hotelproject.availability.entity.PricingRule;
import com.example.hotelproject.availability.entity.RoomAvailability;
import com.example.hotelproject.availability.exception.InvalidDateRangeException;
import com.example.hotelproject.availability.exception.RoomNotAvailableException;
import com.example.hotelproject.availability.exception.PricingRuleNotFoundException;
import com.example.hotelproject.availability.exception.BlockedDateNotFoundException;
import com.example.hotelproject.availability.mapper.AvailabilityMapper;
import com.example.hotelproject.availability.repository.BlockedDateRepository;
import com.example.hotelproject.availability.repository.PricingRuleRepository;
import com.example.hotelproject.availability.repository.RoomAvailabilityRepository;
import com.example.hotelproject.enums.BookingStatus;
import com.example.hotelproject.enums.DayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityServiceImpl.class);
    private static final int HOLD_EXPIRY_MINUTES = 15;

    private final RoomAvailabilityRepository availabilityRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final BlockedDateRepository blockedDateRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;
    private final AvailabilityMapper mapper;

    public AvailabilityServiceImpl(RoomAvailabilityRepository availabilityRepository,
                                   PricingRuleRepository pricingRuleRepository,
                                   BlockedDateRepository blockedDateRepository,
                                   HotelRepository hotelRepository,
                                   RoomTypeRepository roomTypeRepository,
                                   BookingRepository bookingRepository,
                                   AvailabilityMapper mapper) {
        this.availabilityRepository = availabilityRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.blockedDateRepository = blockedDateRepository;
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.bookingRepository = bookingRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponseDto checkAvailability(AvailabilityRequestDto request) {
        log.info("Checking availability: hotelId={}, from={} to={}, guests={}",
                request.getHotelId(), request.getCheckInDate(),
                request.getCheckOutDate(), request.getGuests());

        validateDateRange(request.getCheckInDate(), request.getCheckOutDate());

        Hotel hotel = hotelRepository.findByIdAndActiveTrue(request.getHotelId())
                .orElseThrow(() -> new HotelNotFoundException(request.getHotelId()));

        List<RoomType> roomTypes;
        if (request.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findByIdAndActiveTrue(request.getRoomTypeId())
                    .orElseThrow(() -> new RoomTypeNotFoundException(request.getRoomTypeId()));

            if (!roomType.getHotel().getId().equals(hotel.getId())) {
                throw new IllegalArgumentException("Room type does not belong to the specified hotel");
            }

            roomTypes = Collections.singletonList(roomType);
        } else {
            roomTypes = roomTypeRepository.findByHotelIdAndActiveTrue(hotel.getId());
        }

        roomTypes = roomTypes.stream()
                .filter(rt -> rt.getCapacity() >= request.getGuests())
                .collect(Collectors.toList());

        List<AvailabilityMapper.RoomTypeAvailabilityInfo> roomTypeInfos = new ArrayList<>();

        for (RoomType roomType : roomTypes) {
            AvailabilityMapper.RoomTypeAvailabilityInfo info =
                    checkRoomTypeAvailability(roomType, request);
            roomTypeInfos.add(info);
        }

        roomTypeInfos.sort(Comparator.comparing(AvailabilityMapper.RoomTypeAvailabilityInfo::getTotalPrice));

        return mapper.toAvailabilityResponseDto(
                hotel,
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getGuests(),
                roomTypeInfos
        );
    }

    private AvailabilityMapper.RoomTypeAvailabilityInfo checkRoomTypeAvailability(
            RoomType roomType, AvailabilityRequestDto request) {

        List<LocalDate> dates = getDatesBetween(request.getCheckInDate(), request.getCheckOutDate());

        List<RoomAvailability> availabilities = availabilityRepository
                .findByRoomTypeIdAndDateBetween(roomType.getId(),
                        request.getCheckInDate(),
                        request.getCheckOutDate().minusDays(1));

        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                roomType.getId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                -1L);

        Map<LocalDate, Integer> bookedRoomsPerDay = new HashMap<>();
        for (Booking booking : conflictingBookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PENDING) {
                List<LocalDate> bookingDates = getDatesBetween(
                        booking.getCheckInDate(),
                        booking.getCheckOutDate());
                for (LocalDate date : bookingDates) {
                    bookedRoomsPerDay.merge(date, booking.getNumberOfRooms(), Integer::sum);
                }
            }
        }

        Map<LocalDate, RoomAvailability> availabilityMap = availabilities.stream()
                .collect(Collectors.toMap(RoomAvailability::getDate, ra -> ra));

        boolean allDatesAvailable = true;
        List<AvailabilityMapper.DailyAvailabilityInfo> dailyInfos = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        int minAvailableRooms = Integer.MAX_VALUE;

        for (LocalDate date : dates) {
            RoomAvailability ra = availabilityMap.get(date);

            if (ra == null) {
                ra = new RoomAvailability(roomType, date, roomType.getTotalRooms());
            }

            int bookedRoomsOnDate = bookedRoomsPerDay.getOrDefault(date, 0);
            int totalRooms = ra.getTotalRooms();
            int heldRooms = ra.getHeldRooms();
            int blockedRooms = ra.getBlockedRooms();

            int actualAvailableRooms = totalRooms - bookedRoomsOnDate - heldRooms - blockedRooms;

            boolean isBlocked = blockedDateRepository.isDateBlocked(roomType.getId(), date);
            boolean isAvailable = !isBlocked && actualAvailableRooms >= request.getRoomsRequested();

            if (!isAvailable) {
                allDatesAvailable = false;
            }

            minAvailableRooms = Math.min(minAvailableRooms, actualAvailableRooms);

            BigDecimal priceForDate = calculatePriceForDate(roomType, date);
            totalPrice = totalPrice.add(priceForDate.multiply(BigDecimal.valueOf(request.getRoomsRequested())));

            dailyInfos.add(new AvailabilityMapper.DailyAvailabilityInfo(
                    date,
                    isAvailable,
                    actualAvailableRooms,
                    priceForDate
            ));
        }

        int availableRooms = allDatesAvailable ? minAvailableRooms : 0;

        return new AvailabilityMapper.RoomTypeAvailabilityInfo(
                roomType.getId(),
                roomType.getName(),
                roomType.getCapacity(),
                availableRooms,
                totalPrice,
                dates.size(),
                dailyInfos
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PriceCalculationResponseDto calculatePrice(PriceCalculationRequestDto request) {
        log.info("Calculating price: roomTypeId={}, from={} to={}, rooms={}",
                request.getRoomTypeId(), request.getCheckInDate(),
                request.getCheckOutDate(), request.getNumberOfRooms());

        validateDateRange(request.getCheckInDate(), request.getCheckOutDate());

        RoomType roomType = roomTypeRepository.findByIdAndActiveTrue(request.getRoomTypeId())
                .orElseThrow(() -> new RoomTypeNotFoundException(request.getRoomTypeId()));

        List<LocalDate> dates = getDatesBetween(request.getCheckInDate(), request.getCheckOutDate());
        List<AvailabilityMapper.PriceBreakdownInfo> breakdownInfos = new ArrayList<>();
        List<AvailabilityMapper.AppliedRuleInfo> ruleInfos = new ArrayList<>();

        BigDecimal basePricePerNight = roomType.getBasePrice();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (LocalDate date : dates) {
            List<PricingRule> applicableRules = pricingRuleRepository
                    .findApplicableRulesForDate(roomType.getHotel().getId(), roomType.getId(), date);

            DayType dayType = mapper.getDayType(date);

            BigDecimal adjustedPrice = applyPricingRules(roomType.getBasePrice(), applicableRules, date, dayType);
            BigDecimal priceForDay = adjustedPrice.multiply(BigDecimal.valueOf(request.getNumberOfRooms()));
            totalPrice = totalPrice.add(priceForDay);

            breakdownInfos.add(new AvailabilityMapper.PriceBreakdownInfo(
                    date,
                    roomType.getBasePrice(),
                    adjustedPrice,
                    adjustedPrice,
                    priceForDay
            ));

            if (date.equals(dates.get(0))) {
                for (PricingRule rule : applicableRules) {
                    ruleInfos.add(new AvailabilityMapper.AppliedRuleInfo(
                            rule.getName(),
                            rule.getDescription() != null ? rule.getDescription() : "",
                            calculateAdjustment(rule, roomType.getBasePrice())
                    ));
                }
            }
        }

        BigDecimal finalPricePerNight = totalPrice.divide(
                BigDecimal.valueOf(dates.size()), 2, RoundingMode.HALF_UP);

        return mapper.toPriceCalculationResponseDto(
                roomType,
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getNumberOfRooms(),
                roomType.getBasePrice(),
                finalPricePerNight,
                breakdownInfos,
                ruleInfos
        );
    }

    private BigDecimal calculatePriceForDate(RoomType roomType, LocalDate date) {
        List<PricingRule> applicableRules = pricingRuleRepository
                .findApplicableRulesForDate(roomType.getHotel().getId(), roomType.getId(), date);

        DayType dayType = mapper.getDayType(date);
        return applyPricingRules(roomType.getBasePrice(), applicableRules, date, dayType);
    }

    private BigDecimal applyPricingRules(BigDecimal basePrice, List<PricingRule> rules,
                                         LocalDate date, DayType dayType) {
        if (rules.isEmpty()) {
            return basePrice;
        }

        rules.sort((r1, r2) -> r2.getPriority().compareTo(r1.getPriority()));

        BigDecimal price = basePrice;

        for (PricingRule rule : rules) {
            if (!rule.appliesTo(date, dayType)) {
                continue;
            }

            if (rule.getMultiplier() != null) {
                price = price.multiply(rule.getMultiplier());
            }

            if (rule.getFixedAdjustment() != null) {
                price = price.add(rule.getFixedAdjustment());
            }
        }

        return price.max(BigDecimal.ZERO);
    }

    private BigDecimal calculateAdjustment(PricingRule rule, BigDecimal basePrice) {
        if (rule.getMultiplier() != null) {
            return basePrice.multiply(rule.getMultiplier().subtract(BigDecimal.ONE));
        }
        if (rule.getFixedAdjustment() != null) {
            return rule.getFixedAdjustment();
        }
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public void initializeRoomAvailability(Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        log.info("Initializing availability for roomTypeId={} from {} to {}",
                roomTypeId, startDate, endDate);

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RoomTypeNotFoundException(roomTypeId));

        List<LocalDate> dates = getDatesBetween(startDate, endDate);

        for (LocalDate date : dates) {
            availabilityRepository.findByRoomTypeIdAndDate(roomTypeId, date)
                    .orElseGet(() -> {
                        RoomAvailability availability = new RoomAvailability(
                                roomType, date, roomType.getTotalRooms());
                        return availabilityRepository.save(availability);
                    });
        }
    }

    @Override
    @Transactional
    public void holdRooms(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, int numberOfRooms) {
        log.info("Holding {} rooms for roomTypeId={} from {} to {}",
                numberOfRooms, roomTypeId, checkInDate, checkOutDate);

        List<RoomAvailability> availabilities = getAndLockAvailabilities(
                roomTypeId, checkInDate, checkOutDate);

        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                roomTypeId, checkInDate, checkOutDate, -1L);

        Map<LocalDate, Integer> bookedRoomsPerDay = new HashMap<>();
        for (Booking booking : conflictingBookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PENDING) {
                List<LocalDate> bookingDates = getDatesBetween(
                        booking.getCheckInDate(),
                        booking.getCheckOutDate());
                for (LocalDate date : bookingDates) {
                    bookedRoomsPerDay.merge(date, booking.getNumberOfRooms(), Integer::sum);
                }
            }
        }

        for (RoomAvailability ra : availabilities) {
            int bookedRoomsOnDate = bookedRoomsPerDay.getOrDefault(ra.getDate(), 0);
            int actualAvailableRooms = ra.getTotalRooms() - bookedRoomsOnDate - ra.getHeldRooms() - ra.getBlockedRooms();

            if (actualAvailableRooms < numberOfRooms) {
                throw new RoomNotAvailableException(
                        "Not enough rooms available on " + ra.getDate() +
                                ". Available: " + actualAvailableRooms +
                                ", Requested: " + numberOfRooms);
            }
        }

        for (RoomAvailability ra : availabilities) {
            ra.holdRooms(numberOfRooms);
            availabilityRepository.save(ra);
        }
    }

    @Override
    @Transactional
    public void releaseHold(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, int numberOfRooms) {
        log.info("Releasing hold on {} rooms for roomTypeId={} from {} to {}",
                numberOfRooms, roomTypeId, checkInDate, checkOutDate);

        List<RoomAvailability> availabilities = getAndLockAvailabilities(
                roomTypeId, checkInDate, checkOutDate);

        for (RoomAvailability ra : availabilities) {
            ra.releaseHold(numberOfRooms);
            availabilityRepository.save(ra);
        }
    }

    @Override
    @Transactional
    public void confirmBooking(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, int numberOfRooms) {
        log.info("Confirming booking for {} rooms for roomTypeId={} from {} to {}",
                numberOfRooms, roomTypeId, checkInDate, checkOutDate);

        List<RoomAvailability> availabilities = getAndLockAvailabilities(
                roomTypeId, checkInDate, checkOutDate);

        for (RoomAvailability ra : availabilities) {
            ra.confirmBooking(numberOfRooms);
            availabilityRepository.save(ra);
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, int numberOfRooms) {
        log.info("Cancelling booking for {} rooms for roomTypeId={} from {} to {}",
                numberOfRooms, roomTypeId, checkInDate, checkOutDate);

        List<RoomAvailability> availabilities = getAndLockAvailabilities(
                roomTypeId, checkInDate, checkOutDate);

        for (RoomAvailability ra : availabilities) {
            ra.cancelBooking(numberOfRooms);
            availabilityRepository.save(ra);
        }
    }

    private List<RoomAvailability> getAndLockAvailabilities(
            Long roomTypeId, LocalDate startDate, LocalDate endDate) {

        List<LocalDate> dates = getDatesBetween(startDate, endDate);
        List<RoomAvailability> availabilities = new ArrayList<>();

        for (LocalDate date : dates) {
            RoomAvailability ra = availabilityRepository
                    .findByRoomTypeIdAndDateWithLock(roomTypeId, date)
                    .orElseGet(() -> {
                        RoomType roomType = roomTypeRepository.getReferenceById(roomTypeId);
                        return new RoomAvailability(roomType, date, roomType.getTotalRooms());
                    });

            if (ra.getId() == null) {
                ra = availabilityRepository.save(ra);
            }
            availabilities.add(ra);
        }

        return availabilities;
    }

    @Override
    @Transactional
    public BlockedDateResponseDto blockDates(BlockDateRequestDto request) {
        log.info("Blocking dates: hotelId={}, from={} to={}, reason={}",
                request.getHotelId(), request.getStartDate(), request.getEndDate(), request.getReason());

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new HotelNotFoundException(request.getHotelId()));

        RoomType roomType = null;
        if (request.getRoomTypeId() != null) {
            roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new RoomTypeNotFoundException(request.getRoomTypeId()));

            if (!roomType.getHotel().getId().equals(hotel.getId())) {
                throw new IllegalArgumentException("Room type does not belong to the specified hotel");
            }
        }

        BlockedDate blockedDate = mapper.toEntity(request, hotel, roomType);
        BlockedDate savedBlock = blockedDateRepository.save(blockedDate);

        List<LocalDate> dates = getDatesBetween(request.getStartDate(), request.getEndDate().plusDays(1));

        for (LocalDate date : dates) {
            if (roomType != null) {
                updateAvailabilityForBlock(roomType.getId(), date,
                        request.getRoomsToBlock() != null ? request.getRoomsToBlock() : roomType.getTotalRooms());
            } else {
                List<RoomType> roomTypes = roomTypeRepository.findByHotelId(hotel.getId());
                for (RoomType rt : roomTypes) {
                    updateAvailabilityForBlock(rt.getId(), date,
                            request.getRoomsToBlock() != null ? request.getRoomsToBlock() : rt.getTotalRooms());
                }
            }
        }

        return toBlockedDateResponseDto(savedBlock);
    }

    private void updateAvailabilityForBlock(Long roomTypeId, LocalDate date, int roomsToBlock) {
        RoomAvailability availability = availabilityRepository
                .findByRoomTypeIdAndDateWithLock(roomTypeId, date)
                .orElseGet(() -> {
                    RoomType rt = roomTypeRepository.getReferenceById(roomTypeId);
                    return new RoomAvailability(rt, date, rt.getTotalRooms());
                });

        if (availability.getId() == null) {
            availability = availabilityRepository.save(availability);
        }

        availability.blockRooms(roomsToBlock);
        availabilityRepository.save(availability);
    }

    private BlockedDateResponseDto toBlockedDateResponseDto(BlockedDate blockedDate) {
        return new BlockedDateResponseDto(
                blockedDate.getId(),
                blockedDate.getHotel().getId(),
                blockedDate.getHotel().getName(),
                blockedDate.getRoomType() != null ? blockedDate.getRoomType().getId() : null,
                blockedDate.getRoomType() != null ? blockedDate.getRoomType().getName() : null,
                blockedDate.getStartDate(),
                blockedDate.getEndDate(),
                blockedDate.getReason(),
                blockedDate.getBlockedRoomsCount(),
                blockedDate.isActive(),
                blockedDate.getCreatedAt(),
                blockedDate.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void unblockDates(Long blockedDateId) {
        log.info("Unblocking dates for blockedDateId={}", blockedDateId);

        BlockedDate blockedDate = blockedDateRepository.findById(blockedDateId)
                .orElseThrow(() -> new BlockedDateNotFoundException(blockedDateId));

        blockedDate.setActive(false);
        blockedDateRepository.save(blockedDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockedDateResponseDto> getActiveBlocksForHotel(
            Long hotelId, LocalDate startDate, LocalDate endDate) {

        log.info("Getting active blocks for hotelId={} from {} to {}", hotelId, startDate, endDate);

        List<BlockedDate> blocks = blockedDateRepository
                .findActiveBlocksInDateRange(hotelId, startDate, endDate);

        return blocks.stream()
                .map(this::toBlockedDateResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PricingRuleResponseDto createPricingRule(PricingRuleRequestDto request) {
        log.info("Creating pricing rule: name={}, hotelId={}", request.getName(), request.getHotelId());

        if (request.getHotelId() != null &&
                pricingRuleRepository.existsByNameAndHotelId(request.getName(), request.getHotelId())) {
            throw new IllegalArgumentException("Pricing rule with name '" + request.getName() +
                    "' already exists for this hotel");
        }

        PricingRule rule = new PricingRule();

        if (request.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new HotelNotFoundException(request.getHotelId()));
            rule.setHotel(hotel);
        }

        if (request.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new RoomTypeNotFoundException(request.getRoomTypeId()));
            rule.setRoomType(roomType);
        }

        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setDayType(request.getDayType());
        rule.setStartDate(request.getStartDate());
        rule.setEndDate(request.getEndDate());
        rule.setMultiplier(request.getMultiplier());
        rule.setFixedAdjustment(request.getFixedAdjustment());
        rule.setMinStay(request.getMinStay());
        rule.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        rule.setActive(true);

        PricingRule savedRule = pricingRuleRepository.save(rule);

        return toPricingRuleResponseDto(savedRule);
    }

    @Override
    @Transactional
    public PricingRuleResponseDto updatePricingRule(Long ruleId, PricingRuleRequestDto request) {
        log.info("Updating pricing rule: id={}", ruleId);

        PricingRule rule = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new PricingRuleNotFoundException(ruleId));

        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setDayType(request.getDayType());
        rule.setStartDate(request.getStartDate());
        rule.setEndDate(request.getEndDate());
        rule.setMultiplier(request.getMultiplier());
        rule.setFixedAdjustment(request.getFixedAdjustment());
        rule.setMinStay(request.getMinStay());
        rule.setPriority(request.getPriority() != null ? request.getPriority() : 0);

        PricingRule updatedRule = pricingRuleRepository.save(rule);

        return toPricingRuleResponseDto(updatedRule);
    }

    @Override
    @Transactional
    public void deletePricingRule(Long ruleId) {
        log.info("Deleting pricing rule: id={}", ruleId);

        PricingRule rule = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new PricingRuleNotFoundException(ruleId));

        rule.setActive(false);
        pricingRuleRepository.save(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingRuleResponseDto> getPricingRulesForHotel(Long hotelId) {
        log.info("Getting pricing rules for hotelId={}", hotelId);

        List<PricingRule> rules = pricingRuleRepository.findByHotelIdAndActiveTrue(hotelId);

        return rules.stream()
                .map(this::toPricingRuleResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingRuleResponseDto> getPricingRulesForRoomType(Long roomTypeId) {
        log.info("Getting pricing rules for roomTypeId={}", roomTypeId);

        List<PricingRule> rules = pricingRuleRepository.findByRoomTypeIdAndActiveTrue(roomTypeId);

        return rules.stream()
                .map(this::toPricingRuleResponseDto)
                .collect(Collectors.toList());
    }

    private PricingRuleResponseDto toPricingRuleResponseDto(PricingRule rule) {
        return new PricingRuleResponseDto(
                rule.getId(),
                rule.getHotel() != null ? rule.getHotel().getId() : null,
                rule.getHotel() != null ? rule.getHotel().getName() : null,
                rule.getRoomType() != null ? rule.getRoomType().getId() : null,
                rule.getRoomType() != null ? rule.getRoomType().getName() : null,
                rule.getName(),
                rule.getDescription(),
                rule.getDayType(),
                rule.getStartDate(),
                rule.getEndDate(),
                rule.getMultiplier(),
                rule.getFixedAdjustment(),
                rule.getMinStay(),
                rule.getPriority(),
                rule.isActive(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void bulkUpdateAvailability(Long hotelId, LocalDate startDate, LocalDate endDate,
                                       Integer priceAdjustmentPercentage, Integer roomsToAdd) {
        log.info("Bulk updating availability: hotelId={}, from={} to={}", hotelId, startDate, endDate);

        List<RoomType> roomTypes = roomTypeRepository.findByHotelId(hotelId);
        List<LocalDate> dates = getDatesBetween(startDate, endDate);

        for (RoomType roomType : roomTypes) {
            for (LocalDate date : dates) {
                RoomAvailability availability = availabilityRepository
                        .findByRoomTypeIdAndDateWithLock(roomType.getId(), date)
                        .orElseGet(() -> {
                            RoomAvailability ra = new RoomAvailability(roomType, date, roomType.getTotalRooms());
                            return availabilityRepository.save(ra);
                        });

                if (roomsToAdd != null) {
                    availability.setTotalRooms(availability.getTotalRooms() + roomsToAdd);
                }

                if (priceAdjustmentPercentage != null) {
                    BigDecimal currentPrice = availability.getCustomPrice() != null ?
                            availability.getCustomPrice() : roomType.getBasePrice();
                    BigDecimal adjustment = currentPrice
                            .multiply(BigDecimal.valueOf(priceAdjustmentPercentage))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    availability.setCustomPrice(currentPrice.add(adjustment));
                }

                availabilityRepository.save(availability);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomAvailability> getAvailabilityReport(Long hotelId, LocalDate startDate,
                                                        LocalDate endDate, Pageable pageable) {
        log.info("Generating availability report for hotelId={} from {} to {}", hotelId, startDate, endDate);

        List<RoomAvailability> availabilities = availabilityRepository
                .findByHotelIdAndDateBetween(hotelId, startDate, endDate);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), availabilities.size());

        return new PageImpl<>(
                availabilities.subList(start, end),
                pageable,
                availabilities.size()
        );
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidDateRangeException("Dates cannot be null");
        }
        if (startDate.isAfter(endDate) || startDate.equals(endDate)) {
            throw new InvalidDateRangeException(
                    "Check-out date must be after check-in date");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException(
                    "Check-in date cannot be in the past");
        }
    }

    private List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;
        while (current.isBefore(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }
}
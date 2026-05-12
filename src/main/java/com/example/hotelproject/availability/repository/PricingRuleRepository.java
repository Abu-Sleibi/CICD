package com.example.hotelproject.availability.repository;

import com.example.hotelproject.availability.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    List<PricingRule> findByHotelIdAndActiveTrue(Long hotelId);

    List<PricingRule> findByRoomTypeIdAndActiveTrue(Long roomTypeId);

    @Query("SELECT pr FROM PricingRule pr WHERE pr.active = true AND " +
            "(pr.hotel.id = :hotelId OR pr.hotel IS NULL) AND " +
            "(pr.roomType.id = :roomTypeId OR pr.roomType IS NULL) " +
            "ORDER BY pr.priority DESC")
    List<PricingRule> findApplicableRules(
            @Param("hotelId") Long hotelId,
            @Param("roomTypeId") Long roomTypeId);

    @Query("SELECT pr FROM PricingRule pr WHERE pr.active = true AND " +
            "(pr.hotel.id = :hotelId OR pr.hotel IS NULL) AND " +
            "(pr.roomType.id = :roomTypeId OR pr.roomType IS NULL) AND " +
            "(pr.startDate IS NULL OR pr.startDate <= :date) AND " +
            "(pr.endDate IS NULL OR pr.endDate >= :date)")
    List<PricingRule> findApplicableRulesForDate(
            @Param("hotelId") Long hotelId,
            @Param("roomTypeId") Long roomTypeId,
            @Param("date") LocalDate date);

    boolean existsByNameAndHotelId(String name, Long hotelId);
}
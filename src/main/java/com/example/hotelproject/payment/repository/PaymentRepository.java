package com.example.hotelproject.payment.repository;

import com.example.hotelproject.payment.entity.Payment;
import com.example.hotelproject.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    List<Payment> findByBookingId(Long bookingId);

    Page<Payment> findByBookingId(Long bookingId, Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);
}
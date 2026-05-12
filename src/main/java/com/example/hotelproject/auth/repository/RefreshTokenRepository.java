package com.example.hotelproject.auth.repository;

import com.example.hotelproject.auth.entity.RefreshToken;
import com.example.hotelproject.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteAllExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Transactional
    void deleteByUser(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByToken(String token);
}
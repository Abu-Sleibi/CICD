package com.example.hotelproject.auth.service;

import com.example.hotelproject.auth.entity.RefreshToken;
import com.example.hotelproject.auth.entity.User;
import com.example.hotelproject.auth.exception.RefreshTokenException;
import com.example.hotelproject.auth.repository.RefreshTokenRepository;
import com.example.hotelproject.auth.repository.UserRepository;
import com.example.hotelproject.auth.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new RefreshTokenException("Refresh token was revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException("Refresh token expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(Instant.now());
    }
}
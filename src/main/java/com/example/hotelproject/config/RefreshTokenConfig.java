package com.example.hotelproject.config;

import com.example.hotelproject.auth.service.RefreshTokenService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class RefreshTokenConfig {

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenConfig(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
        refreshTokenService.cleanupExpiredTokens();
    }
}
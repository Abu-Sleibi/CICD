package com.example.hotelproject.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequestDto {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public LogoutRequestDto() {
    }

    public LogoutRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
package com.example.hotelproject.auth.dto;

import com.example.hotelproject.auth.entity.Role;
import java.util.Set;

public class LoginResponseDto {

    private final String accessToken;
    private final String refreshToken;
    private final String type = "Bearer";
    private final Long id;
    private final Long guestId;
    private final String username;
    private final String email;
    private final String fullName;
    private final Set<Role> roles;
    private final Long hotelId;

    public LoginResponseDto(String accessToken, String refreshToken, Long id, Long guestId, String username,
                            String email, String fullName, Set<Role> roles, Long hotelId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.guestId = guestId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.hotelId = hotelId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public Long getGuestId() {
        return guestId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Long getHotelId() {
        return hotelId;
    }
}
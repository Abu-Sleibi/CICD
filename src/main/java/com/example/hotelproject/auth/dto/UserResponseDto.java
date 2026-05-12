package com.example.hotelproject.auth.dto;

import com.example.hotelproject.auth.entity.Role;
import java.time.LocalDateTime;
import java.util.Set;

public class UserResponseDto {

    private final Long id;
    private final String username;
    private final String email;
    private final String fullName;
    private final String phone;
    private final Set<Role> roles;
    private final Long hotelId;
    private final boolean enabled;
    private final LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;

    public UserResponseDto(Long id, String username, String email, String fullName,
                           String phone, Set<Role> roles, Long hotelId, boolean enabled,
                           LocalDateTime lastLoginAt, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.roles = roles;
        this.hotelId = hotelId;
        this.enabled = enabled;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public Set<Role> getRoles() { return roles; }
    public Long getHotelId() { return hotelId; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
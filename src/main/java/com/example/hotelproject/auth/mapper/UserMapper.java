package com.example.hotelproject.auth.mapper;

import com.example.hotelproject.auth.dto.RegisterRequestDto;
import com.example.hotelproject.auth.dto.UserResponseDto;
import com.example.hotelproject.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequestDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        return user;
    }

    public UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRoles(),
                user.getHotelId(),
                user.isEnabled(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
package com.example.hotelproject.auth.service;

import com.example.hotelproject.auth.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthService {

    LoginResponseDto login(LoginRequestDto request);

    UserResponseDto register(RegisterRequestDto request);

    UserResponseDto registerHotelManager(RegisterRequestDto request, Long hotelId);

    UserResponseDto getCurrentUser(String username);

    UserResponseDto getUserById(Long id);

    Page<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto updateUser(Long id, RegisterRequestDto request);

    void deleteUser(Long id);

    void changePassword(Long userId, ChangePasswordRequestDto request);

    void toggleUserStatus(Long userId, boolean enabled);

    void addRoleToUser(Long userId, String role);

    void removeRoleFromUser(Long userId, String role);

    LoginResponseDto refreshToken(String refreshToken);

    void logout(String refreshToken);
}
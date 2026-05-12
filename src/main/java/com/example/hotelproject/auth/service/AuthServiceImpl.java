package com.example.hotelproject.auth.service;

import com.example.hotelproject.auth.dto.*;
import com.example.hotelproject.auth.entity.Role;
import com.example.hotelproject.auth.entity.User;
import com.example.hotelproject.auth.entity.RefreshToken;
import com.example.hotelproject.auth.exception.UnauthorizedException;
import com.example.hotelproject.auth.exception.UserNotFoundException;
import com.example.hotelproject.auth.exception.RefreshTokenException;
import com.example.hotelproject.auth.mapper.UserMapper;
import com.example.hotelproject.auth.repository.UserRepository;
import com.example.hotelproject.auth.security.JwtService;
import com.example.hotelproject.booking.entity.Guest;
import com.example.hotelproject.booking.repository.GuestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserRepository userRepository,
                           GuestRepository guestRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.guestRepository = guestRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new UnauthorizedException("Invalid username or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        Guest guest = guestRepository.findByEmail(user.getEmail()).orElse(null);
        Long guestId = guest != null ? guest.getId() : null;

        String accessToken = jwtService.generateAccessToken((UserDetails) authentication.getPrincipal());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new LoginResponseDto(
                accessToken,
                refreshToken.getToken(),
                user.getId(),
                guestId,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles(),
                user.getHotelId()
        );
    }

    @Override
    public UserResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Role> roles = new HashSet<>();
        roles.add(Role.GUEST);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    @Override
    public UserResponseDto registerHotelManager(RegisterRequestDto request, Long hotelId) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Role> roles = new HashSet<>();
        roles.add(Role.HOTEL_MANAGER);
        user.setRoles(roles);
        user.setHotelId(hotelId);

        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    public UserResponseDto updateUser(Long id, RegisterRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setEnabled(false);
        refreshTokenService.revokeAllUserTokens(id);
        userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequestDto request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        refreshTokenService.revokeAllUserTokens(userId);
        userRepository.save(user);
    }

    @Override
    public void toggleUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setEnabled(enabled);
        if (!enabled) {
            refreshTokenService.revokeAllUserTokens(userId);
        }
        userRepository.save(user);
    }

    @Override
    public void addRoleToUser(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        try {
            Role newRole = Role.valueOf(role.toUpperCase());
            user.getRoles().add(newRole);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    @Override
    public void removeRoleFromUser(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        try {
            Role removeRole = Role.valueOf(role.toUpperCase());
            user.getRoles().remove(removeRole);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    @Override
    public LoginResponseDto refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenService.verifyRefreshToken(refreshToken);
        User user = token.getUser();

        Guest guest = guestRepository.findByEmail(user.getEmail()).orElse(null);
        Long guestId = guest != null ? guest.getId() : null;

        String newAccessToken = jwtService.generateAccessToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities(user.getRoles().stream()
                                .map(role -> "ROLE_" + role.name())
                                .toArray(String[]::new))
                        .build()
        );

        return new LoginResponseDto(
                newAccessToken,
                refreshToken,
                user.getId(),
                guestId,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles(),
                user.getHotelId()
        );
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}
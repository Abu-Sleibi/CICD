package com.example.hotelproject.auth;

import com.example.hotelproject.auth.dto.*;
import com.example.hotelproject.auth.entity.RefreshToken;
import com.example.hotelproject.auth.entity.Role;
import com.example.hotelproject.auth.entity.User;
import com.example.hotelproject.auth.exception.UnauthorizedException;
import com.example.hotelproject.auth.exception.UserNotFoundException;
import com.example.hotelproject.auth.mapper.UserMapper;
import com.example.hotelproject.auth.repository.RefreshTokenRepository;
import com.example.hotelproject.auth.repository.UserRepository;
import com.example.hotelproject.auth.security.JwtService;
import com.example.hotelproject.auth.service.AuthServiceImpl;
import com.example.hotelproject.auth.service.RefreshTokenService;
import com.example.hotelproject.booking.entity.Guest;
import com.example.hotelproject.booking.repository.GuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private Guest guest;
    private RefreshToken refreshToken;
    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private ChangePasswordRequestDto changePasswordRequest;
    private UserResponseDto userResponse;
    private RefreshTokenRequestDto refreshTokenRequest;
    private LogoutRequestDto logoutRequest;

    @BeforeEach
    void setUp() {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.GUEST);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFullName("Test User");
        user.setRoles(roles);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        guest = new Guest();
        guest.setId(5L);
        guest.setEmail("test@example.com");
        guest.setFullName("Test User");

        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUser(user);
        refreshToken.setToken("refresh-token-123");
        refreshToken.setExpiryDate(Instant.now().plusSeconds(604800));
        refreshToken.setRevoked(false);

        registerRequest = new RegisterRequestDto();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");

        loginRequest = new LoginRequestDto();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");

        changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setCurrentPassword("password123");
        changePasswordRequest.setNewPassword("newpassword456");
        changePasswordRequest.setConfirmPassword("newpassword456");

        refreshTokenRequest = new RefreshTokenRequestDto();
        refreshTokenRequest.setRefreshToken("refresh-token-123");

        logoutRequest = new LogoutRequestDto();
        logoutRequest.setRefreshToken("refresh-token-123");

        userResponse = new UserResponseDto(1L, "testuser", "test@example.com",
                "Test User", null, user.getRoles(), null, true,
                null, LocalDateTime.now());
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponseDto result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(user);
    }

    @Test
    void register_ThrowsException_WhenUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ThrowsException_WhenEmailExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        RefreshToken mockRefreshToken = mock(RefreshToken.class);
        lenient().when(mockRefreshToken.getToken()).thenReturn("refresh-token-123");

        lenient().when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn("testuser");
        lenient().when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.of(user));
        lenient().when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token-123");
        lenient().when(refreshTokenService.createRefreshToken(1L)).thenReturn(mockRefreshToken);

        LoginResponseDto result = authService.login(loginRequest);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
        verify(guestRepository).findByEmail(user.getEmail());
    }

    @Test
    void refreshToken_Success() {
        when(refreshTokenService.verifyRefreshToken("refresh-token-123")).thenReturn(refreshToken);
        when(guestRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(guest));
        when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("new-access-token-456");

        LoginResponseDto result = authService.refreshToken("refresh-token-123");

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token-456");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token-123");
        verify(guestRepository).findByEmail(user.getEmail());
    }

    @Test
    void logout_Success() {
        doNothing().when(refreshTokenService).revokeRefreshToken("refresh-token-123");

        authService.logout("refresh-token-123");

        verify(refreshTokenService).revokeRefreshToken("refresh-token-123");
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponseDto result = authService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getUserById_ThrowsException_WhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    void getCurrentUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponseDto result = authService.getCurrentUser("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getCurrentUser_ThrowsException_WhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void changePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newpassword456")).thenReturn("newEncodedPassword");
        doNothing().when(refreshTokenService).revokeAllUserTokens(1L);

        authService.changePassword(1L, changePasswordRequest);

        verify(userRepository).save(user);
        verify(refreshTokenService).revokeAllUserTokens(1L);
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    void changePassword_ThrowsException_WhenPasswordsDoNotMatch() {
        changePasswordRequest.setConfirmPassword("different");

        assertThatThrownBy(() -> authService.changePassword(1L, changePasswordRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passwords do not match");

        verify(userRepository, never()).save(any(User.class));
        verify(refreshTokenService, never()).revokeAllUserTokens(anyLong());
    }

    @Test
    void changePassword_ThrowsException_WhenCurrentPasswordIncorrect() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(1L, changePasswordRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any(User.class));
        verify(refreshTokenService, never()).revokeAllUserTokens(anyLong());
    }

    @Test
    void toggleUserStatus_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.toggleUserStatus(1L, false);

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).save(user);
        verify(refreshTokenService).revokeAllUserTokens(1L);
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(refreshTokenService).revokeAllUserTokens(1L);

        authService.deleteUser(1L);

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).save(user);
        verify(refreshTokenService).revokeAllUserTokens(1L);
    }

    @Test
    void addRoleToUser_Success() {
        Set<Role> originalRoles = user.getRoles();
        assertThat(originalRoles).hasSize(1);
        assertThat(originalRoles).contains(Role.GUEST);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.addRoleToUser(1L, "ADMIN");

        assertThat(user.getRoles()).hasSize(2);
        assertThat(user.getRoles()).contains(Role.GUEST, Role.ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    void addRoleToUser_ThrowsException_WhenInvalidRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.addRoleToUser(1L, "INVALID_ROLE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role");

        verify(userRepository, never()).save(any(User.class));
    }
}
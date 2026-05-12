package com.example.hotelproject.auth.controller;

import com.example.hotelproject.auth.dto.*;
import com.example.hotelproject.auth.security.CurrentUser;
import com.example.hotelproject.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "01. Authentication", description = "User registration, login, and profile management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "User Login",
            description = "Authenticate user and return JWT token. username or email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful - returns JWT token"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        LoginResponseDto response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDto request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request,
            UriComponentsBuilder uriBuilder) {

        UserResponseDto response = authService.register(request);

        URI location = uriBuilder
                .path("/api/v1/auth/users/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/register/manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> registerHotelManager(
            @Valid @RequestBody RegisterRequestDto request,
            @RequestParam Long hotelId,
            UriComponentsBuilder uriBuilder) {

        UserResponseDto response = authService.registerHotelManager(request, hotelId);

        URI location = uriBuilder
                .path("/api/v1/auth/users/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@CurrentUser UserDetails userDetails) {
        UserResponseDto response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto response = authService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<UserResponseDto> users = authService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/test")
    public String test() {
        return "Auth controller is working!";
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody RegisterRequestDto request) {

        UserResponseDto response = authService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/change-password")
    @PreAuthorize("@userSecurity.isCurrentUser(#id)")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequestDto request) {

        authService.changePassword(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {

        authService.toggleUserStatus(id, enabled);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addRoleToUser(
            @PathVariable Long id,
            @PathVariable String role) {

        authService.addRoleToUser(id, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable Long id,
            @PathVariable String role) {

        authService.removeRoleFromUser(id, role);
        return ResponseEntity.noContent().build();
    }
}
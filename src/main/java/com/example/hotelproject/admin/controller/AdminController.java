package com.example.hotelproject.admin.controller;

import com.example.hotelproject.admin.dto.SystemStatisticsDto;
import com.example.hotelproject.admin.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "09. Admin Dashboard", description = "Platform-wide statistics and admin operations")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemStatisticsDto> getSystemStatistics() {
        return ResponseEntity.ok(adminService.getSystemStatistics());
    }
}

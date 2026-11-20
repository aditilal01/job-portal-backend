package com.jobportal.controller;

import com.jobportal.dto.response.ApiResponse;
import com.jobportal.dto.response.UserResponse;
import com.jobportal.dto.response.AdminStatsResponse;
import com.jobportal.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/admin", "/api"})
@Tag(name = "Admin", description = "Admin-only endpoints for platform management")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform-wide administration statistics")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getAdminStats() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAdminStats()));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Admin endpoint to list all registered users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID", description = "Admin endpoint to view a specific user")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }
}

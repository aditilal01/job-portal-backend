package com.jobportal.service;

import com.jobportal.dto.response.UserResponse;
import com.jobportal.dto.response.AdminStatsResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    /**
     * Get all users (Admin only).
     *
     * @return list of user responses
     */
    List<UserResponse> getAllUsers();

    /**
     * Get a single user by ID.
     *
     * @param userId user ID
     * @return user response
     */
    UserResponse getUserById(UUID userId);
    AdminStatsResponse getAdminStats();
}

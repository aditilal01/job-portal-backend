package com.jobportal.service.impl;

import com.jobportal.dto.response.UserResponse;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.UserRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.entity.UserRole;
import com.jobportal.dto.response.AdminStatsResponse;
import com.jobportal.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of user management operations (primarily for Admin use).
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public UserServiceImpl(UserRepository userRepository, JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .candidates(userRepository.countByRole(UserRole.CANDIDATE))
                .recruiters(userRepository.countByRole(UserRole.RECRUITER))
                .admins(userRepository.countByRole(UserRole.ADMIN))
                .totalJobs(jobRepository.count())
                .activeJobs(jobRepository.countByIsActiveTrue())
                .totalApplications(applicationRepository.count())
                .build();
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

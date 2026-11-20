package com.jobportal.controller;

import com.jobportal.dto.request.CandidateProfileRequest;
import com.jobportal.dto.response.ApiResponse;
import com.jobportal.dto.response.CandidateProfileResponse;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.CandidateProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/candidate/profile")
@Tag(name = "Candidate Profile", description = "Candidate-owned professional profile")
public class CandidateProfileController {
    private final CandidateProfileService profileService;
    private final UserRepository userRepository;

    public CandidateProfileController(CandidateProfileService profileService, UserRepository userRepository) {
        this.profileService = profileService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Get the authenticated candidate's profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(profileService.getProfile(resolveCandidateId(userDetails))));
    }

    @PutMapping
    @Operation(summary = "Create or update the authenticated candidate's profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> updateProfile(
            @Valid @RequestBody CandidateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CandidateProfileResponse response = profileService.updateProfile(resolveCandidateId(userDetails), request);
        return ResponseEntity.ok(ApiResponse.success("Candidate profile saved successfully", response));
    }

    private UUID resolveCandidateId(UserDetails userDetails) {
        User candidate = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        return candidate.getId();
    }
}

package com.jobportal.controller;

import com.jobportal.dto.response.ApiResponse;
import com.jobportal.dto.response.SavedJobResponse;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.SavedJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidate/saved-jobs")
@Tag(name = "Saved Jobs", description = "Candidate job bookmarks")
public class SavedJobController {
    private final SavedJobService savedJobService;
    private final UserRepository userRepository;

    public SavedJobController(SavedJobService savedJobService, UserRepository userRepository) {
        this.savedJobService = savedJobService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{jobId}")
    @Operation(summary = "Save an active job to the candidate's bookmarks")
    public ResponseEntity<ApiResponse<SavedJobResponse>> saveJob(@PathVariable UUID jobId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Job saved successfully", savedJobService.saveJob(resolveCandidateId(principal), jobId)));
    }

    @GetMapping
    @Operation(summary = "List the authenticated candidate's saved jobs")
    public ResponseEntity<ApiResponse<List<SavedJobResponse>>> getSavedJobs(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(savedJobService.getSavedJobs(resolveCandidateId(principal))));
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Remove a job from the candidate's bookmarks")
    public ResponseEntity<ApiResponse<Void>> removeSavedJob(@PathVariable UUID jobId,
            @AuthenticationPrincipal UserDetails principal) {
        savedJobService.removeSavedJob(resolveCandidateId(principal), jobId);
        return ResponseEntity.ok(ApiResponse.success("Saved job removed successfully", null));
    }

    private UUID resolveCandidateId(UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getUsername()));
        return user.getId();
    }
}
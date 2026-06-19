package com.jobportal.controller;

import com.jobportal.dto.request.StatusUpdateRequest;
import com.jobportal.dto.response.ApiResponse;
import com.jobportal.dto.response.ApplicationResponse;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications", description = "Job application endpoints")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public ApplicationController(ApplicationService applicationService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Apply to a job", description = "Candidates can apply to a job with an optional resume file upload")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToJob(
            @RequestParam("jobId") UUID jobId,
            @RequestParam(value = "resume", required = false) MultipartFile resumeFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID candidateId = resolveUserId(userDetails);
        ApplicationResponse response = applicationService.applyToJob(jobId, candidateId, resumeFile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", response));
    }

    @GetMapping("/my")
    @Operation(summary = "Candidate dashboard", description = "Returns all applications submitted by the logged-in candidate")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getCandidateApplications(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID candidateId = resolveUserId(userDetails);
        List<ApplicationResponse> applications = applicationService.getCandidateApplications(candidateId);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get applicants for a job", description = "Recruiters can view all applications for their job posting")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getJobApplications(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID recruiterId = resolveUserId(userDetails);
        List<ApplicationResponse> applications = applicationService.getJobApplications(jobId, recruiterId);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update application status", description = "Recruiters can update an application's status through the defined workflow")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID recruiterId = resolveUserId(userDetails);
        ApplicationResponse response = applicationService.updateApplicationStatus(id, request.getStatus(), recruiterId);
        return ResponseEntity.ok(ApiResponse.success("Application status updated", response));
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private UUID resolveUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        return user.getId();
    }
}

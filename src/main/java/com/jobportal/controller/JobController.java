package com.jobportal.controller;

import com.jobportal.dto.request.JobRequest;
import com.jobportal.dto.response.ApiResponse;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.RecruiterDashboardResponse;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Jobs", description = "Job posting CRUD and search endpoints")
public class JobController {

    private final JobService jobService;
    private final UserRepository userRepository;

    public JobController(JobService jobService, UserRepository userRepository) {
        this.jobService = jobService;
        this.userRepository = userRepository;
    }

    @PostMapping("/jobs")
    @Operation(summary = "Create a job posting", description = "Recruiters can create a new job posting")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID recruiterId = resolveUserId(userDetails);
        JobResponse response = jobService.createJob(request, recruiterId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", response));
    }

    @PutMapping("/jobs/{id}")
    @Operation(summary = "Update a job posting", description = "Recruiters can update their own job postings")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable UUID id,
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID recruiterId = resolveUserId(userDetails);
        JobResponse response = jobService.updateJob(id, request, recruiterId);
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", response));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Delete a job posting", description = "Recruiters can soft-delete their own job postings")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID recruiterId = resolveUserId(userDetails);
        jobService.deleteJob(id, recruiterId);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }

    @GetMapping("/jobs/{id}")
    @Operation(summary = "Get a job by ID", description = "Public endpoint to view a specific job posting")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable UUID id) {
        JobResponse response = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/jobs")
    @Operation(summary = "Search jobs", description = "Public endpoint to search and filter active job postings with pagination")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> searchJobs(
            @Parameter(description = "Filter by skill") @RequestParam(required = false) String skill,
            @Parameter(description = "Filter by location") @RequestParam(required = false) String location,
            @Parameter(description = "Filter by max experience required") @RequestParam(required = false) Integer experience,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<JobResponse> results = jobService.searchJobs(skill, location, experience, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/recruiter/dashboard")
    @Operation(summary = "Recruiter dashboard", description = "Returns all jobs posted by the recruiter with applicant counts")
    public ResponseEntity<ApiResponse<List<RecruiterDashboardResponse>>> getRecruiterDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID recruiterId = resolveUserId(userDetails);
        List<RecruiterDashboardResponse> dashboard = jobService.getRecruiterDashboard(recruiterId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private UUID resolveUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        return user.getId();
    }
}

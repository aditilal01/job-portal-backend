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
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Jobs", description = "Job posting CRUD and search endpoints")
public class JobController {
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("createdAt", "title", "location", "experienceRequired");

    private final JobService jobService;
    private final UserRepository userRepository;

    public JobController(JobService jobService, UserRepository userRepository) {
        this.jobService = jobService;
        this.userRepository = userRepository;
    }

    @PostMapping("/jobs")
    @Operation(summary = "Create a job posting")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(@Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobResponse response = jobService.createJob(request, resolveUserId(userDetails));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", response));
    }

    @PutMapping("/jobs/{id}")
    @Operation(summary = "Update a job posting")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(@PathVariable UUID id,
            @Valid @RequestBody JobRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        JobResponse response = jobService.updateJob(id, request, resolveUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", response));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Soft-delete a job posting")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobService.deleteJob(id, resolveUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }

    @GetMapping("/jobs/{id}")
    @Operation(summary = "Get an active public job by ID")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getJobById(id)));
    }

    @GetMapping("/recruiter/jobs/{id}")
    @Operation(summary = "Get one of the authenticated recruiter's jobs, including inactive jobs")
    public ResponseEntity<ApiResponse<JobResponse>> getRecruiterJobById(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                jobService.getRecruiterJobById(id, resolveUserId(userDetails))));
    }

    @GetMapping("/jobs")
    @Operation(summary = "Search active jobs with pagination")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> searchJobs(
            @Parameter(description = "Filter by job title") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by skill or keyword") @RequestParam(required = false) String skill,
            @Parameter(description = "Filter by location") @RequestParam(required = false) String location,
            @Parameter(description = "Maximum experience required") @RequestParam(required = false) Integer experience,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        if (page < 0) throw new IllegalArgumentException("Page index cannot be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");
        if (experience != null && experience < 0) throw new IllegalArgumentException("Experience cannot be negative");
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Unsupported sort field. Allowed: " + ALLOWED_SORT_FIELDS);
        }
        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
        }
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<JobResponse> results = jobService.searchJobs(title, skill, location, experience, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/recruiter/dashboard")
    @Operation(summary = "Recruiter dashboard")
    public ResponseEntity<ApiResponse<List<RecruiterDashboardResponse>>> getRecruiterDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<RecruiterDashboardResponse> dashboard = jobService.getRecruiterDashboard(resolveUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    private UUID resolveUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        return user.getId();
    }
}

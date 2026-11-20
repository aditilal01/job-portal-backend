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
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Operation(summary = "Apply to a job", description = "Candidates can apply with an optional PDF, DOC, or DOCX resume")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToJob(@RequestParam("jobId") UUID jobId,
            @RequestParam(value = "resume", required = false) MultipartFile resumeFile,
            @AuthenticationPrincipal UserDetails userDetails) {
        ApplicationResponse response = applicationService.applyToJob(jobId, resolveUserId(userDetails), resumeFile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getCandidateApplications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getCandidateApplications(resolveUserId(userDetails))));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getJobApplications(@PathVariable UUID jobId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getJobApplications(jobId, resolveUserId(userDetails))));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(@PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        ApplicationResponse response = applicationService.updateApplicationStatus(
                id, request.getStatus(), resolveUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Application status updated", response));
    }

    @GetMapping("/{id}/resume")
    @Operation(summary = "Download an application resume (candidate owner or job-owning recruiter)")
    public ResponseEntity<Resource> downloadResume(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User user = resolveUser(userDetails);
        Path path = applicationService.getResumePath(id, user.getId(), user.getRole().name().equals("RECRUITER"));
        Resource resource = new UrlResource(path.toUri());
        String contentType = FilesProbe.contentType(path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
                .body(resource);
    }

    private UUID resolveUserId(UserDetails userDetails) {
        return resolveUser(userDetails).getId();
    }

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
    }

    private static final class FilesProbe {
        private static String contentType(Path path) throws IOException {
            String type = java.nio.file.Files.probeContentType(path);
            return type == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : type;
        }
    }
}

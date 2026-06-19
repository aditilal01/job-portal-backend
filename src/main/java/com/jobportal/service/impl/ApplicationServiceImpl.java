package com.jobportal.service.impl;

import com.jobportal.dto.response.ApplicationResponse;
import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.exception.*;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of application operations: apply, status updates, and dashboards.
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final String uploadDir;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  JobRepository jobRepository,
                                  UserRepository userRepository,
                                  @Value("${app.file.upload-dir}") String uploadDir) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.uploadDir = uploadDir;
    }

    @Override
    @Transactional
    public ApplicationResponse applyToJob(UUID jobId, UUID candidateId, MultipartFile resumeFile) {
        // Verify job exists and is active
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (!job.getIsActive()) {
            throw new IllegalArgumentException("Cannot apply to an inactive job posting");
        }

        // Verify candidate exists
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", candidateId));

        // Check for duplicate application
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidateId)) {
            throw new DuplicateApplicationException(
                    "You have already applied to this job: " + job.getTitle());
        }

        // Handle resume file upload
        String resumeUrl = null;
        if (resumeFile != null && !resumeFile.isEmpty()) {
            resumeUrl = saveResumeFile(resumeFile, candidateId, jobId);
        }

        // Create and save application
        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .resumeUrl(resumeUrl)
                .status(ApplicationStatus.APPLIED)
                .build();

        application = applicationRepository.save(application);
        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(UUID applicationId, String newStatus, UUID recruiterId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        // Verify the recruiter owns the job this application belongs to
        if (!application.getJob().getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedActionException("You are not authorized to update this application");
        }

        // Parse and validate the target status
        ApplicationStatus targetStatus;
        try {
            targetStatus = ApplicationStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        // Validate state machine transition
        ApplicationStatus currentStatus = application.getStatus();
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new InvalidStatusTransitionException(currentStatus.name(), targetStatus.name());
        }

        application.setStatus(targetStatus);
        application = applicationRepository.save(application);

        log.info("Application {} status updated: {} → {}", applicationId, currentStatus, targetStatus);
        return mapToResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getCandidateApplications(UUID candidateId) {
        return applicationRepository.findByCandidateIdOrderByAppliedAtDesc(candidateId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getJobApplications(UUID jobId, UUID recruiterId) {
        // Verify job exists and recruiter owns it
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (!job.getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedActionException("You are not authorized to view applicants for this job");
        }

        return applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Helper Methods ───────────────────────────────────────────────────────

    private String saveResumeFile(MultipartFile file, UUID candidateId, UUID jobId) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = "resume_" + candidateId + "_" + jobId + extension;
            Path targetPath = Paths.get(uploadDir).resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Resume saved: {}", targetPath.toAbsolutePath());
            return targetPath.toString();
        } catch (IOException e) {
            log.error("Failed to save resume file", e);
            throw new RuntimeException("Failed to upload resume file", e);
        }
    }

    private ApplicationResponse mapToResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .candidateId(application.getCandidate().getId())
                .candidateName(application.getCandidate().getName())
                .candidateEmail(application.getCandidate().getEmail())
                .resumeUrl(application.getResumeUrl())
                .status(application.getStatus().name())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}

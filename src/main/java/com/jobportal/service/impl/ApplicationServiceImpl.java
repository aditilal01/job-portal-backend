package com.jobportal.service.impl;

import com.jobportal.dto.response.ApplicationResponse;
import com.jobportal.dto.response.CandidateProfileSummaryResponse;
import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.exception.*;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.CandidateProfileRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.ApplicationService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private static final long MAX_RESUME_BYTES = 10L * 1024 * 1024;
    private final ApplicationRepository applicationRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private String uploadDir;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository, JobRepository jobRepository,
            UserRepository userRepository, CandidateProfileRepository candidateProfileRepository,
            @Value("${app.file.upload-dir}") String uploadDir) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.uploadDir = uploadDir;
    }

    @Override
    @Transactional
    public ApplicationResponse applyToJob(UUID jobId, UUID candidateId, MultipartFile resumeFile) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        if (!Boolean.TRUE.equals(job.getIsActive())) {
            throw new IllegalArgumentException("Cannot apply to an inactive job posting");
        }
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", candidateId));
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidateId)) {
            throw new DuplicateApplicationException("You have already applied to this job: " + job.getTitle());
        }

        String storedResume = resumeFile == null || resumeFile.isEmpty()
                ? null : saveResumeFile(resumeFile);
        Application application = Application.builder().job(job).candidate(candidate)
                .resumeUrl(storedResume).status(ApplicationStatus.APPLIED).build();
        application = applicationRepository.save(application);
        return mapToResponse(application, false);
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(UUID applicationId, String newStatus, UUID recruiterId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));
        if (!application.getJob().getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedActionException("You are not authorized to update this application");
        }
        ApplicationStatus target;
        try {
            target = ApplicationStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }
        ApplicationStatus current = application.getStatus();
        if (!current.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(current.name(), target.name());
        }
        application.setStatus(target);
        log.info("Application {} status updated: {} -> {}", applicationId, current, target);
        return mapToResponse(applicationRepository.save(application), true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getCandidateApplications(UUID candidateId) {
        return applicationRepository.findByCandidateIdOrderByAppliedAtDesc(candidateId)
                .stream().map(application -> mapToResponse(application, false)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getJobApplications(UUID jobId, UUID recruiterId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        if (!job.getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedActionException("You are not authorized to view applicants for this job");
        }
        return applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId)
                .stream().map(application -> mapToResponse(application, true)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Path getResumePath(UUID applicationId, UUID requesterId, boolean recruiter) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));
        boolean candidateOwner = application.getCandidate().getId().equals(requesterId);
        boolean recruiterOwner = application.getJob().getPostedBy().getId().equals(requesterId);
        if (!(candidateOwner || (recruiter && recruiterOwner))) {
            throw new UnauthorizedActionException("You are not authorized to access this resume");
        }
        if (application.getResumeUrl() == null || application.getResumeUrl().isBlank()) {
            throw new ResourceNotFoundException("Resume", "applicationId", applicationId);
        }
        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path path = uploadRoot.resolve(application.getResumeUrl()).normalize();
        if (!path.startsWith(uploadRoot) || !Files.isRegularFile(path)) {
            throw new ResourceNotFoundException("Resume", "applicationId", applicationId);
        }
        return path;
    }

    private String saveResumeFile(MultipartFile file) {
        String original = file.getOriginalFilename();
        String extension = original == null ? "" : original.substring(original.lastIndexOf('.') + 1)
                .toLowerCase(java.util.Locale.ROOT);
        if (!List.of("pdf", "doc", "docx").contains(extension)) {
            throw new IllegalArgumentException("Resume must be a PDF, DOC, or DOCX file");
        }
        if (file.getSize() > MAX_RESUME_BYTES) {
            throw new IllegalArgumentException("Resume must not exceed 10 MB");
        }
        String contentType = file.getContentType();
        boolean validType = switch (extension) {
            case "pdf" -> "application/pdf".equalsIgnoreCase(contentType);
            case "doc" -> "application/msword".equalsIgnoreCase(contentType)
                    || "application/octet-stream".equalsIgnoreCase(contentType);
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equalsIgnoreCase(contentType)
                    || "application/octet-stream".equalsIgnoreCase(contentType);
            default -> false;
        };
        if (!validType) throw new IllegalArgumentException("Resume file type does not match its extension");
        String filename = UUID.randomUUID() + "." + extension;
        try {
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadRoot);
            Path destination = uploadRoot.resolve(filename).normalize();
            if (!destination.startsWith(uploadRoot)) throw new IllegalArgumentException("Invalid resume filename");
            try (var input = file.getInputStream()) {
                Files.copy(input, destination);
            }
            return filename;
        } catch (IOException ex) {
            log.error("Failed to save resume", ex);
            throw new IllegalStateException("Failed to upload resume file", ex);
        }
    }

    private ApplicationResponse mapToResponse(Application application, boolean includeCandidateProfile) {
        String resumeUrl = application.getResumeUrl() == null ? null
                : "/api/applications/" + application.getId() + "/resume";
        CandidateProfileSummaryResponse candidateProfile = null;
        if (includeCandidateProfile) {
            candidateProfile = candidateProfileRepository.findByUser_Id(application.getCandidate().getId())
                    .filter(com.jobportal.entity.CandidateProfile::isProfileVisibleToRecruiters)
                    .map(profile -> CandidateProfileSummaryResponse.builder()
                            .headline(profile.getHeadline()).location(profile.getLocation())
                            .summary(profile.getSummary()).skills(profile.getSkills())
                            .education(profile.getEducation()).experience(profile.getExperience())
                            .portfolioUrl(profile.getPortfolioUrl()).linkedInUrl(profile.getLinkedInUrl())
                            .githubUrl(profile.getGithubUrl()).build())
                    .orElse(null);
        }
        return ApplicationResponse.builder().id(application.getId()).jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle()).candidateId(application.getCandidate().getId())
                .candidateName(application.getCandidate().getName()).candidateEmail(application.getCandidate().getEmail())
                .candidateProfile(candidateProfile).resumeUrl(resumeUrl).status(application.getStatus().name()).appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt()).build();
    }
}

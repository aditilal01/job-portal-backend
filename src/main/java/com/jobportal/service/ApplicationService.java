package com.jobportal.service;

import com.jobportal.dto.response.ApplicationResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationService {
    ApplicationResponse applyToJob(UUID jobId, UUID candidateId, MultipartFile resumeFile);
    ApplicationResponse updateApplicationStatus(UUID applicationId, String newStatus, UUID recruiterId);
    List<ApplicationResponse> getCandidateApplications(UUID candidateId);
    List<ApplicationResponse> getJobApplications(UUID jobId, UUID recruiterId);
    Path getResumePath(UUID applicationId, UUID requesterId, boolean recruiter);
}

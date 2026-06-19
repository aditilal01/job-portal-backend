package com.jobportal.service;

import com.jobportal.dto.response.ApplicationResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ApplicationService {

    /**
     * Submit a job application with an optional resume file.
     *
     * @param jobId the job to apply for
     * @param candidateId the candidate applying
     * @param resumeFile optional resume file upload
     * @return the created application response
     */
    ApplicationResponse applyToJob(UUID jobId, UUID candidateId, MultipartFile resumeFile);

    /**
     * Update an application's status (recruiter action).
     *
     * @param applicationId ID of the application
     * @param newStatus target status string
     * @param recruiterId ID of the recruiter performing the update
     * @return updated application response
     */
    ApplicationResponse updateApplicationStatus(UUID applicationId, String newStatus, UUID recruiterId);

    /**
     * Get all applications submitted by a candidate.
     *
     * @param candidateId candidate's user ID
     * @return list of application responses
     */
    List<ApplicationResponse> getCandidateApplications(UUID candidateId);

    /**
     * Get all applications for a specific job (recruiter action).
     *
     * @param jobId job ID
     * @param recruiterId recruiter's user ID (for ownership verification)
     * @return list of application responses
     */
    List<ApplicationResponse> getJobApplications(UUID jobId, UUID recruiterId);
}

package com.jobportal.service;

import com.jobportal.dto.request.JobRequest;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.RecruiterDashboardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface JobService {

    /**
     * Create a new job posting.
     *
     * @param request job details
     * @param recruiterId UUID of the recruiter creating the job
     * @return created job response
     */
    JobResponse createJob(JobRequest request, UUID recruiterId);

    /**
     * Update an existing job posting.
     *
     * @param jobId ID of the job to update
     * @param request updated job details
     * @param recruiterId UUID of the requesting recruiter (for ownership verification)
     * @return updated job response
     */
    JobResponse updateJob(UUID jobId, JobRequest request, UUID recruiterId);

    /**
     * Soft-delete a job posting (set isActive = false).
     *
     * @param jobId ID of the job to delete
     * @param recruiterId UUID of the requesting recruiter (for ownership verification)
     */
    void deleteJob(UUID jobId, UUID recruiterId);

    /**
     * Get a single job by ID.
     *
     * @param jobId job ID
     * @return job response
     */
    JobResponse getJobById(UUID jobId);

    /**
     * Search and filter active jobs with pagination.
     *
     * @param skill optional skill filter
     * @param location optional location filter
     * @param experience optional max experience filter
     * @param pageable pagination and sorting parameters
     * @return paginated job results
     */
    Page<JobResponse> searchJobs(String skill, String location, Integer experience, Pageable pageable);

    /**
     * Get recruiter dashboard data: all jobs posted by this recruiter with applicant counts.
     *
     * @param recruiterId UUID of the recruiter
     * @return list of dashboard entries
     */
    List<RecruiterDashboardResponse> getRecruiterDashboard(UUID recruiterId);
}

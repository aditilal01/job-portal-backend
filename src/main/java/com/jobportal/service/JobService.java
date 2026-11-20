package com.jobportal.service;

import com.jobportal.dto.request.JobRequest;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.RecruiterDashboardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface JobService {
    JobResponse createJob(JobRequest request, UUID recruiterId);
    JobResponse updateJob(UUID jobId, JobRequest request, UUID recruiterId);
    void deleteJob(UUID jobId, UUID recruiterId);
    JobResponse getJobById(UUID jobId);
    JobResponse getRecruiterJobById(UUID jobId, UUID recruiterId);
    Page<JobResponse> searchJobs(String title, String skill, String location, Integer experience, Pageable pageable);
    List<RecruiterDashboardResponse> getRecruiterDashboard(UUID recruiterId);
}

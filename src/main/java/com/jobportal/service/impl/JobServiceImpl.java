package com.jobportal.service.impl;

import com.jobportal.dto.request.JobRequest;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.RecruiterDashboardResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.exception.UnauthorizedActionException;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.JobService;
import com.jobportal.specification.JobSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    public JobServiceImpl(JobRepository jobRepository, UserRepository userRepository,
                          ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    @Transactional
    public JobResponse createJob(JobRequest request, UUID recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recruiterId));
        Job job = Job.builder()
                .title(request.getTitle()).description(request.getDescription())
                .requiredSkills(request.getRequiredSkills()).location(request.getLocation())
                .experienceRequired(request.getExperienceRequired()).salaryRange(request.getSalaryRange())
                .postedBy(recruiter).isActive(true).build();
        return mapToResponse(jobRepository.save(job));
    }

    @Override
    @Transactional
    public JobResponse updateJob(UUID jobId, JobRequest request, UUID recruiterId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        verifyOwnership(job, recruiterId);
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequiredSkills(request.getRequiredSkills());
        job.setLocation(request.getLocation());
        job.setExperienceRequired(request.getExperienceRequired());
        job.setSalaryRange(request.getSalaryRange());
        return mapToResponse(jobRepository.save(job));
    }

    @Override
    @Transactional
    public void deleteJob(UUID jobId, UUID recruiterId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        verifyOwnership(job, recruiterId);
        job.setIsActive(false);
        jobRepository.save(job);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        if (!Boolean.TRUE.equals(job.getIsActive())) {
            throw new ResourceNotFoundException("Active job", "id", jobId);
        }
        return mapToResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getRecruiterJobById(UUID jobId, UUID recruiterId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        verifyOwnership(job, recruiterId);
        return mapToResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String title, String skill, String location, Integer experience, Pageable pageable) {
        Specification<Job> spec = Specification.where(JobSpecification.isActive());
        if (title != null && !title.isBlank()) spec = spec.and(JobSpecification.hasTitle(title));
        if (skill != null && !skill.isBlank()) spec = spec.and(JobSpecification.hasSkill(skill));
        if (location != null && !location.isBlank()) spec = spec.and(JobSpecification.hasLocation(location));
        if (experience != null) spec = spec.and(JobSpecification.hasMaxExperience(experience));
        return jobRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecruiterDashboardResponse> getRecruiterDashboard(UUID recruiterId) {
        List<Job> jobs = jobRepository.findByPostedByIdOrderByCreatedAtDesc(recruiterId);
        return jobs.stream().map(job -> RecruiterDashboardResponse.builder()
                .jobId(job.getId()).jobTitle(job.getTitle()).location(job.getLocation())
                .isActive(job.getIsActive()).applicantCount(applicationRepository.countByJobId(job.getId()))
                .createdAt(job.getCreatedAt()).build()).collect(Collectors.toList());
    }

    private void verifyOwnership(Job job, UUID recruiterId) {
        if (!job.getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedActionException("You are not authorized to access this job posting");
        }
    }

    private JobResponse mapToResponse(Job job) {
        return JobResponse.builder().id(job.getId()).title(job.getTitle()).description(job.getDescription())
                .requiredSkills(job.getRequiredSkills()).location(job.getLocation())
                .experienceRequired(job.getExperienceRequired()).salaryRange(job.getSalaryRange())
                .recruiterName(job.getPostedBy().getName()).recruiterId(job.getPostedBy().getId())
                .isActive(job.getIsActive()).createdAt(job.getCreatedAt()).build();
    }
}

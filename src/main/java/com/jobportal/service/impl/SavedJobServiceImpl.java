package com.jobportal.service.impl;

import com.jobportal.dto.response.SavedJobResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.SavedJob;
import com.jobportal.entity.User;
import com.jobportal.entity.UserRole;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.SavedJobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.SavedJobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class SavedJobServiceImpl implements SavedJobService {
    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public SavedJobServiceImpl(SavedJobRepository savedJobRepository, JobRepository jobRepository,
                               UserRepository userRepository) {
        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public SavedJobResponse saveJob(UUID candidateId, UUID jobId) {
        User candidate = getCandidate(candidateId);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        if (!Boolean.TRUE.equals(job.getIsActive())) {
            throw new IllegalArgumentException("This job is no longer active and cannot be saved.");
        }
        SavedJob saved = savedJobRepository.findByCandidate_IdAndJob_Id(candidateId, jobId)
                .orElseGet(() -> savedJobRepository.save(SavedJob.builder().candidate(candidate).job(job).build()));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedJobResponse> getSavedJobs(UUID candidateId) {
        getCandidate(candidateId);
        return savedJobRepository.findByCandidate_IdOrderBySavedAtDesc(candidateId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void removeSavedJob(UUID candidateId, UUID jobId) {
        getCandidate(candidateId);
        if (!savedJobRepository.existsByCandidate_IdAndJob_Id(candidateId, jobId)) {
            throw new ResourceNotFoundException("Saved job", "jobId", jobId);
        }
        savedJobRepository.deleteByCandidate_IdAndJob_Id(candidateId, jobId);
    }

    private User getCandidate(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        if (user.getRole() != UserRole.CANDIDATE) {
            throw new IllegalArgumentException("Only candidate accounts can manage saved jobs.");
        }
        return user;
    }

    private SavedJobResponse toResponse(SavedJob saved) {
        Job job = saved.getJob();
        return SavedJobResponse.builder().id(saved.getId()).jobId(job.getId()).title(job.getTitle())
                .description(job.getDescription()).requiredSkills(job.getRequiredSkills())
                .location(job.getLocation()).experienceRequired(job.getExperienceRequired())
                .salaryRange(job.getSalaryRange()).recruiterName(job.getPostedBy().getName())
                .isActive(job.getIsActive()).jobPostedAt(job.getCreatedAt()).savedAt(saved.getSavedAt()).build();
    }
}
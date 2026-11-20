package com.jobportal.service;

import com.jobportal.dto.response.SavedJobResponse;
import java.util.List;
import java.util.UUID;

public interface SavedJobService {
    SavedJobResponse saveJob(UUID candidateId, UUID jobId);
    List<SavedJobResponse> getSavedJobs(UUID candidateId);
    void removeSavedJob(UUID candidateId, UUID jobId);
}
package com.jobportal.repository;

import com.jobportal.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedJobRepository extends JpaRepository<SavedJob, UUID> {
    List<SavedJob> findByCandidate_IdOrderBySavedAtDesc(UUID candidateId);
    Optional<SavedJob> findByCandidate_IdAndJob_Id(UUID candidateId, UUID jobId);
    boolean existsByCandidate_IdAndJob_Id(UUID candidateId, UUID jobId);
    void deleteByCandidate_IdAndJob_Id(UUID candidateId, UUID jobId);
}
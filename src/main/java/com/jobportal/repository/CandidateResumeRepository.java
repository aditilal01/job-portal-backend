package com.jobportal.repository;

import com.jobportal.entity.CandidateResume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CandidateResumeRepository extends JpaRepository<CandidateResume, UUID> {
    Optional<CandidateResume> findByUser_Id(UUID userId);
}

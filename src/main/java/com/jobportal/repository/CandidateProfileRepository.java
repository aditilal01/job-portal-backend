package com.jobportal.repository;

import com.jobportal.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, UUID> {
    /**
     * Resolve the profile through the associated User primary key.
     * The underscore makes the nested-property traversal explicit to Spring Data.
     */
    Optional<CandidateProfile> findByUser_Id(UUID userId);
}

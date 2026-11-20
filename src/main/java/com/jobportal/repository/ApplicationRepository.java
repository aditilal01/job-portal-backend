package com.jobportal.repository;

import com.jobportal.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByCandidateIdOrderByAppliedAtDesc(UUID candidateId);

    List<Application> findByJobIdOrderByAppliedAtDesc(UUID jobId);

    boolean existsByJobIdAndCandidateId(UUID jobId, UUID candidateId);

    long count();

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId")
    long countByJobId(@Param("jobId") UUID jobId);

    @Query("SELECT a FROM Application a JOIN FETCH a.job j WHERE j.postedBy.id = :recruiterId ORDER BY a.appliedAt DESC")
    List<Application> findAllByRecruiterIdWithJob(@Param("recruiterId") UUID recruiterId);
}

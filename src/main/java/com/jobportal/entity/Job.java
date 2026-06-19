package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a job posting created by a Recruiter.
 */
@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_job_location", columnList = "location"),
        @Index(name = "idx_job_active", columnList = "is_active"),
        @Index(name = "idx_job_posted_by", columnList = "posted_by_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "required_skills", nullable = false, length = 500)
    private String requiredSkills;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(name = "experience_required", nullable = false)
    private Integer experienceRequired;

    @Column(name = "salary_range", length = 50)
    private String salaryRange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_id", nullable = false)
    private User postedBy;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

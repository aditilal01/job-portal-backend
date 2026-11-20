package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 120)
    private String headline;

    @Column(length = 30)
    private String phone;

    @Column(length = 120)
    private String location;

    @Column(length = 2000)
    private String summary;

    @Column(length = 1000)
    private String skills;

    @Column(length = 2000)
    private String education;

    @Column(length = 3000)
    private String experience;

    @Column(length = 500)
    private String portfolioUrl;

    @Column(length = 500)
    private String linkedInUrl;

    @Column(length = 500)
    private String githubUrl;

    @Builder.Default
    @Column(name = "profile_visible_to_recruiters", nullable = false, columnDefinition = "boolean default false")
    private boolean profileVisibleToRecruiters = false;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }
}

package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "candidate_resumes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CandidateResume {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 255)
    private String storedFilename;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String extractedText;

    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void updateTimestamp() { updatedAt = LocalDateTime.now(); }
}

package com.jobportal.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedJobResponse {
    private UUID id;
    private UUID jobId;
    private String title;
    private String description;
    private String requiredSkills;
    private String location;
    private Integer experienceRequired;
    private String salaryRange;
    private String recruiterName;
    private Boolean isActive;
    private LocalDateTime jobPostedAt;
    private LocalDateTime savedAt;
}
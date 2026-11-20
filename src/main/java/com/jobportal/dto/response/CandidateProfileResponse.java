package com.jobportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Candidate profile response")
public class CandidateProfileResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private String email;
    private String headline;
    private String phone;
    private String location;
    private String summary;
    private String skills;
    private String education;
    private String experience;
    private String portfolioUrl;
    private String linkedInUrl;
    private String githubUrl;
    private boolean profileVisibleToRecruiters;
    private LocalDateTime updatedAt;
}

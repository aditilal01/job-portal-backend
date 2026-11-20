package com.jobportal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Candidate professional profile details")
public class CandidateProfileRequest {
    @Size(max = 120, message = "Headline must not exceed 120 characters")
    private String headline;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    @Size(max = 120, message = "Location must not exceed 120 characters")
    private String location;

    @Size(max = 2000, message = "Summary must not exceed 2000 characters")
    private String summary;

    @Size(max = 1000, message = "Skills must not exceed 1000 characters")
    private String skills;

    @Size(max = 2000, message = "Education must not exceed 2000 characters")
    private String education;

    @Size(max = 3000, message = "Experience must not exceed 3000 characters")
    private String experience;

    @Size(max = 500, message = "Portfolio URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Portfolio URL must start with http:// or https://")
    private String portfolioUrl;

    @Size(max = 500, message = "LinkedIn URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "LinkedIn URL must start with http:// or https://")
    private String linkedInUrl;

    private boolean profileVisibleToRecruiters;

    @Size(max = 500, message = "GitHub URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "GitHub URL must start with http:// or https://")
    private String githubUrl;
}

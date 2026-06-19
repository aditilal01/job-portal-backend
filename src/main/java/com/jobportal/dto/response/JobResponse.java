package com.jobportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response payload representing a job posting")
public class JobResponse {

    @Schema(description = "Unique job ID")
    private UUID id;

    @Schema(description = "Job title")
    private String title;

    @Schema(description = "Detailed job description")
    private String description;

    @Schema(description = "Required skills (comma-separated)")
    private String requiredSkills;

    @Schema(description = "Job location")
    private String location;

    @Schema(description = "Minimum years of experience required")
    private Integer experienceRequired;

    @Schema(description = "Salary range")
    private String salaryRange;

    @Schema(description = "Name of the recruiter who posted this job")
    private String recruiterName;

    @Schema(description = "Recruiter's user ID")
    private UUID recruiterId;

    @Schema(description = "Whether the job is currently active")
    private Boolean isActive;

    @Schema(description = "When the job was posted")
    private LocalDateTime createdAt;
}

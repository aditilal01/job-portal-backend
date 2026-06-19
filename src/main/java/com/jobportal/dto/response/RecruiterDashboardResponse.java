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
@Schema(description = "An entry in the recruiter dashboard showing a job and its applicant count")
public class RecruiterDashboardResponse {

    @Schema(description = "Job ID")
    private UUID jobId;

    @Schema(description = "Job title")
    private String jobTitle;

    @Schema(description = "Job location")
    private String location;

    @Schema(description = "Whether the job is active")
    private Boolean isActive;

    @Schema(description = "Number of applicants for this job")
    private Long applicantCount;

    @Schema(description = "When the job was posted")
    private LocalDateTime createdAt;
}

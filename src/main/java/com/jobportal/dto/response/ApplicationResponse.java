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
@Schema(description = "Response payload representing an application")
public class ApplicationResponse {

    @Schema(description = "Application ID")
    private UUID id;

    @Schema(description = "Job ID this application is for")
    private UUID jobId;

    @Schema(description = "Job title")
    private String jobTitle;

    @Schema(description = "Candidate's user ID")
    private UUID candidateId;

    @Schema(description = "Candidate's name")
    private String candidateName;

    @Schema(description = "Candidate's email")
    private String candidateEmail;

    @Schema(description = "URL or path to the uploaded resume")
    private String resumeUrl;

    private CandidateProfileSummaryResponse candidateProfile;

    @Schema(description = "Current application status")
    private String status;

    @Schema(description = "When the application was submitted")
    private LocalDateTime appliedAt;

    @Schema(description = "When the application was last updated")
    private LocalDateTime updatedAt;
}

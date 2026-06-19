package com.jobportal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating or updating a job posting")
public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(description = "Job title", example = "Senior Java Developer")
    private String title;

    @NotBlank(message = "Job description is required")
    @Schema(description = "Detailed job description", example = "We are looking for a senior Java developer with microservices experience...")
    private String description;

    @NotBlank(message = "Required skills are required")
    @Size(max = 500, message = "Skills must not exceed 500 characters")
    @Schema(description = "Comma-separated list of required skills", example = "Java, Spring Boot, Microservices, PostgreSQL")
    private String requiredSkills;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    @Schema(description = "Job location", example = "Bangalore, India")
    private String location;

    @NotNull(message = "Experience required is mandatory")
    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience cannot exceed 50 years")
    @Schema(description = "Minimum years of experience required", example = "3")
    private Integer experienceRequired;

    @Size(max = 50, message = "Salary range must not exceed 50 characters")
    @Schema(description = "Salary range (optional)", example = "12-18 LPA")
    private String salaryRange;
}

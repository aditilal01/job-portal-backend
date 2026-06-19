package com.jobportal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for applying to a job (resume is uploaded separately as multipart)")
public class ApplicationRequest {

    @NotNull(message = "Job ID is required")
    @Schema(description = "UUID of the job to apply for", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID jobId;
}

package com.jobportal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating an application's status")
public class StatusUpdateRequest {

    @NotBlank(message = "New status is required")
    @Schema(description = "Target application status", example = "SHORTLISTED",
            allowableValues = {"SHORTLISTED", "INTERVIEW", "REJECTED", "HIRED"})
    private String status;
}

package com.jobportal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for user registration")
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Full name of the user", example = "Jane Doe")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(description = "Email address (used as login credential)", example = "jane.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "Password (min 8 chars)", example = "SecureP@ss1")
    private String password;

    @NotBlank(message = "Role is required")
    @Schema(description = "User role: CANDIDATE or RECRUITER", example = "CANDIDATE", allowableValues = {"CANDIDATE", "RECRUITER"})
    private String role;
}

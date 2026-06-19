package com.jobportal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for user login")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(description = "Registered email address", example = "jane.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "SecureP@ss1")
    private String password;
}

package com.jobportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response returned after successful authentication")
public class AuthResponse {

    @Schema(description = "JWT access token")
    private String token;

    @Builder.Default
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Authenticated user's ID")
    private UUID userId;

    @Schema(description = "User's full name")
    private String name;

    @Schema(description = "User's role")
    private String role;
}

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
@Schema(description = "Response payload representing a user")
public class UserResponse {

    @Schema(description = "User ID")
    private UUID id;

    @Schema(description = "Full name")
    private String name;

    @Schema(description = "Email address")
    private String email;

    @Schema(description = "User role")
    private String role;

    @Schema(description = "Account creation date")
    private LocalDateTime createdAt;
}

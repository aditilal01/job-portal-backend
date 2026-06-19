package com.jobportal.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 * Adds JWT Bearer authentication scheme so all endpoints can be tested via Swagger UI.
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Job Portal API")
                        .description("Production-quality REST API for a Job Portal application. "
                                + "Supports Candidate, Recruiter, and Admin roles with JWT authentication.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Job Portal Team")
                                .email("admin@jobportal.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token. Obtain one by calling POST /api/auth/login")));
    }
}


package com.jobportal.config;

import com.jobportal.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 6 configuration.
 * Uses stateless JWT authentication with role-based access control.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Read allowed frontend origins from application configuration.
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF because authentication is stateless and uses JWT.
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Prevent server-side HTTP session creation.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Role-based authorization.
                .authorizeHttpRequests(auth -> auth

                        // Public authentication endpoints.
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public job browsing.
                        .requestMatchers(HttpMethod.GET,
                                "/api/jobs",
                                "/api/jobs/**"
                        ).permitAll()

                        // Swagger / OpenAPI endpoints.
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()

                        // Recruiter endpoints.
                        .requestMatchers(HttpMethod.POST, "/api/jobs")
                        .hasRole("RECRUITER")

                        .requestMatchers(HttpMethod.PUT, "/api/jobs/**")
                        .hasRole("RECRUITER")

                        .requestMatchers(HttpMethod.DELETE, "/api/jobs/**")
                        .hasRole("RECRUITER")

                        .requestMatchers("/api/recruiter/**")
                        .hasRole("RECRUITER")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/applications/*/status"
                        ).hasRole("RECRUITER")

                        .requestMatchers(HttpMethod.GET,
                                "/api/applications/job/**"
                        ).hasRole("RECRUITER")

                        // Candidate endpoints.
                        .requestMatchers(HttpMethod.POST, "/api/applications")
                        .hasRole("CANDIDATE")

                        .requestMatchers("/api/applications/my")
                        .hasRole("CANDIDATE")

                        // Admin endpoints.
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // All remaining endpoints require authentication.
                        .anyRequest().authenticated()
                )

                // Execute JWT authentication before username/password authentication.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configurable CORS configuration.
     * Origins are supplied through application.yml or environment variables.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Convert comma-separated origins into a list.
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

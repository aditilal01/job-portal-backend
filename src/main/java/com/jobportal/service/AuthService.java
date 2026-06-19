package com.jobportal.service;

import com.jobportal.dto.request.LoginRequest;
import com.jobportal.dto.request.RegisterRequest;
import com.jobportal.dto.response.AuthResponse;

public interface AuthService {

    /**
     * Register a new user (Candidate or Recruiter).
     *
     * @param request registration payload
     * @return authentication response with JWT token
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate an existing user.
     *
     * @param request login payload
     * @return authentication response with JWT token
     */
    AuthResponse login(LoginRequest request);
}

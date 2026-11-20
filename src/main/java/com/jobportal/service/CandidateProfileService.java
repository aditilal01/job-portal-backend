package com.jobportal.service;

import com.jobportal.dto.request.CandidateProfileRequest;
import com.jobportal.dto.response.CandidateProfileResponse;

import java.util.UUID;

public interface CandidateProfileService {
    CandidateProfileResponse getProfile(UUID candidateId);
    CandidateProfileResponse updateProfile(UUID candidateId, CandidateProfileRequest request);
}

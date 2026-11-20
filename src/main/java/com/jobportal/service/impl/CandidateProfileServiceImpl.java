package com.jobportal.service.impl;

import com.jobportal.dto.request.CandidateProfileRequest;
import com.jobportal.dto.response.CandidateProfileResponse;
import com.jobportal.entity.CandidateProfile;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.CandidateProfileRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.CandidateProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CandidateProfileServiceImpl implements CandidateProfileService {
    private final CandidateProfileRepository profileRepository;
    private final UserRepository userRepository;

    public CandidateProfileServiceImpl(CandidateProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateProfileResponse getProfile(UUID candidateId) {
        User candidate = getCandidate(candidateId);
        return profileRepository.findByUser_Id(candidateId)
                .map(this::toResponse)
                .orElseGet(() -> CandidateProfileResponse.builder()
                        .userId(candidate.getId())
                        .name(candidate.getName())
                        .email(candidate.getEmail())
                        .build());
    }

    @Override
    @Transactional
    public CandidateProfileResponse updateProfile(UUID candidateId, CandidateProfileRequest request) {
        User candidate = getCandidate(candidateId);
        CandidateProfile profile = profileRepository.findByUser_Id(candidateId)
                .orElseGet(() -> CandidateProfile.builder().user(candidate).build());

        profile.setHeadline(clean(request.getHeadline()));
        profile.setPhone(clean(request.getPhone()));
        profile.setLocation(clean(request.getLocation()));
        profile.setSummary(clean(request.getSummary()));
        profile.setSkills(clean(request.getSkills()));
        profile.setEducation(clean(request.getEducation()));
        profile.setExperience(clean(request.getExperience()));
        profile.setPortfolioUrl(clean(request.getPortfolioUrl()));
        profile.setLinkedInUrl(clean(request.getLinkedInUrl()));
        profile.setGithubUrl(clean(request.getGithubUrl()));
        profile.setProfileVisibleToRecruiters(request.isProfileVisibleToRecruiters());

        return toResponse(profileRepository.save(profile));
    }

    private User getCandidate(UUID candidateId) {
        User user = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", candidateId));
        if (user.getRole() != com.jobportal.entity.UserRole.CANDIDATE) {
            throw new IllegalArgumentException("Only candidate accounts can access candidate profiles");
        }
        return user;
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CandidateProfileResponse toResponse(CandidateProfile profile) {
        User user = profile.getUser();
        return CandidateProfileResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .headline(profile.getHeadline())
                .phone(profile.getPhone())
                .location(profile.getLocation())
                .summary(profile.getSummary())
                .skills(profile.getSkills())
                .education(profile.getEducation())
                .experience(profile.getExperience())
                .portfolioUrl(profile.getPortfolioUrl())
                .linkedInUrl(profile.getLinkedInUrl())
                .githubUrl(profile.getGithubUrl())
                .profileVisibleToRecruiters(profile.isProfileVisibleToRecruiters())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}

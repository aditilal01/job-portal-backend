package com.jobportal.controller;

import com.jobportal.dto.response.*;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.CandidateAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidate/ai")
@Tag(name = "Candidate AI", description = "AI-powered resume analysis and job matching")
public class CandidateAiController {
    private final CandidateAiService aiService;
    private final UserRepository userRepository;

    public CandidateAiController(CandidateAiService aiService, UserRepository userRepository) {
        this.aiService = aiService;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/resume/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and analyze a candidate resume using AI")
    public ResponseEntity<ApiResponse<AiResumeAnalysisResponse>> analyzeResume(
            @RequestParam("resume") MultipartFile resume,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success("Resume analyzed successfully",
                aiService.analyzeResume(resolveCandidateId(principal), resume)));
    }

    @GetMapping("/job-matches")
    @Operation(summary = "Get AI-ranked jobs based on the candidate's uploaded resume")
    public ResponseEntity<ApiResponse<AiJobMatchResponse>> getJobMatches(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(aiService.getJobMatches(resolveCandidateId(principal))));
    }

    private UUID resolveCandidateId(UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getUsername()));
        return user.getId();
    }
}

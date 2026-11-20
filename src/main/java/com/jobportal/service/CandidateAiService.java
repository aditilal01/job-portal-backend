package com.jobportal.service;

import com.jobportal.dto.response.AiJobMatchResponse;
import com.jobportal.dto.response.AiResumeAnalysisResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface CandidateAiService {
    AiResumeAnalysisResponse analyzeResume(UUID candidateId, MultipartFile resume);
    AiJobMatchResponse getJobMatches(UUID candidateId);
}

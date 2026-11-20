package com.jobportal.dto.response;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AiResumeAnalysisResponse {
    private String resumeFilename;
    private String professionalSummary;
    private List<String> skills;
    private List<String> strengths;
    private List<String> improvementAreas;
    private String experienceLevel;
}

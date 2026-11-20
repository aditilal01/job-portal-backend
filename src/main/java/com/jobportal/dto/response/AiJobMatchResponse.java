package com.jobportal.dto.response;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AiJobMatchResponse {
    private List<JobMatch> matches;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class JobMatch {
        private UUID jobId;
        private String jobTitle;
        private int matchScore;
        private List<String> matchingSkills;
        private List<String> missingSkills;
        private String explanation;
    }
}

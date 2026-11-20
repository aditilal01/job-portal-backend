package com.jobportal.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileSummaryResponse {
    private String headline;
    private String location;
    private String summary;
    private String skills;
    private String education;
    private String experience;
    private String portfolioUrl;
    private String linkedInUrl;
    private String githubUrl;
}
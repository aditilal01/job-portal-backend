package com.jobportal.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long candidates;
    private long recruiters;
    private long admins;
    private long totalJobs;
    private long activeJobs;
    private long totalApplications;
}
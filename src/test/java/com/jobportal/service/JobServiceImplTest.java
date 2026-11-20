package com.jobportal.service;

import com.jobportal.dto.request.JobRequest;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.RecruiterDashboardResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.entity.UserRole;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.exception.UnauthorizedActionException;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.impl.JobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobServiceImpl Unit Tests")
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private JobServiceImpl jobService;

    private User recruiter;
    private User otherRecruiter;
    private Job job;
    private JobRequest jobRequest;

    @BeforeEach
    void setUp() {
        recruiter = User.builder()
                .id(UUID.randomUUID())
                .name("John Recruiter")
                .email("recruiter@test.com")
                .password("encoded")
                .role(UserRole.RECRUITER)
                .createdAt(LocalDateTime.now())
                .build();

        otherRecruiter = User.builder()
                .id(UUID.randomUUID())
                .name("Other Recruiter")
                .email("other@test.com")
                .password("encoded")
                .role(UserRole.RECRUITER)
                .createdAt(LocalDateTime.now())
                .build();

        job = Job.builder()
                .id(UUID.randomUUID())
                .title("Senior Java Developer")
                .description("Looking for a senior Java developer")
                .requiredSkills("Java, Spring Boot, PostgreSQL")
                .location("Bangalore")
                .experienceRequired(5)
                .salaryRange("15-25 LPA")
                .postedBy(recruiter)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        jobRequest = JobRequest.builder()
                .title("Senior Java Developer")
                .description("Looking for a senior Java developer")
                .requiredSkills("Java, Spring Boot, PostgreSQL")
                .location("Bangalore")
                .experienceRequired(5)
                .salaryRange("15-25 LPA")
                .build();
    }

    @Nested
    @DisplayName("createJob")
    class CreateJobTests {

        @Test
        @DisplayName("should create a job successfully")
        void shouldCreateJobSuccessfully() {
            when(userRepository.findById(recruiter.getId())).thenReturn(Optional.of(recruiter));
            when(jobRepository.save(any(Job.class))).thenReturn(job);

            JobResponse response = jobService.createJob(jobRequest, recruiter.getId());

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Senior Java Developer");
            assertThat(response.getRecruiterName()).isEqualTo("John Recruiter");
            verify(jobRepository).save(any(Job.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when recruiter not found")
        void shouldThrowWhenRecruiterNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.createJob(jobRequest, unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }
    }

    @Nested
    @DisplayName("updateJob")
    class UpdateJobTests {

        @Test
        @DisplayName("should update a job successfully when recruiter is owner")
        void shouldUpdateJobSuccessfully() {
            JobRequest updateRequest = JobRequest.builder()
                    .title("Updated Title")
                    .description("Updated description")
                    .requiredSkills("Java, Spring, Kubernetes")
                    .location("Mumbai")
                    .experienceRequired(7)
                    .salaryRange("20-30 LPA")
                    .build();

            when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
            when(jobRepository.save(any(Job.class))).thenReturn(job);

            JobResponse response = jobService.updateJob(job.getId(), updateRequest, recruiter.getId());

            assertThat(response).isNotNull();
            verify(jobRepository).save(any(Job.class));
        }

        @Test
        @DisplayName("should throw UnauthorizedActionException when different recruiter tries to update")
        void shouldThrowWhenNotOwner() {
            when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

            assertThatThrownBy(() -> jobService.updateJob(job.getId(), jobRequest, otherRecruiter.getId()))
                    .isInstanceOf(UnauthorizedActionException.class)
                    .hasMessageContaining("not authorized");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when job not found")
        void shouldThrowWhenJobNotFound() {
            UUID unknownJobId = UUID.randomUUID();
            when(jobRepository.findById(unknownJobId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.updateJob(unknownJobId, jobRequest, recruiter.getId()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteJob")
    class DeleteJobTests {

        @Test
        @DisplayName("should soft-delete a job successfully")
        void shouldDeleteJobSuccessfully() {
            when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
            when(jobRepository.save(any(Job.class))).thenReturn(job);

            jobService.deleteJob(job.getId(), recruiter.getId());

            assertThat(job.getIsActive()).isFalse();
            verify(jobRepository).save(job);
        }

        @Test
        @DisplayName("should throw UnauthorizedActionException when non-owner tries to delete")
        void shouldThrowWhenNonOwnerDeletes() {
            when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

            assertThatThrownBy(() -> jobService.deleteJob(job.getId(), otherRecruiter.getId()))
                    .isInstanceOf(UnauthorizedActionException.class);
        }
    }

    @Nested
    @DisplayName("searchJobs")
    class SearchJobsTests {

        @Test
        @DisplayName("should search jobs with filters and return paginated results")
        @SuppressWarnings("unchecked")
        void shouldSearchJobsWithFilters() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Job> jobPage = new PageImpl<>(List.of(job), pageable, 1);

            when(jobRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(jobPage);

            Page<JobResponse> result = jobService.searchJobs(null, "Java", "Bangalore", 5, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Senior Java Developer");
            verify(jobRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("should search jobs without filters")
        @SuppressWarnings("unchecked")
        void shouldSearchJobsWithoutFilters() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Job> jobPage = new PageImpl<>(List.of(job), pageable, 1);

            when(jobRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(jobPage);

            Page<JobResponse> result = jobService.searchJobs(null, null, null, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getRecruiterDashboard")
    class RecruiterDashboardTests {

        @Test
        @DisplayName("should return recruiter dashboard with applicant counts")
        void shouldReturnDashboard() {
            when(jobRepository.findByPostedByIdOrderByCreatedAtDesc(recruiter.getId()))
                    .thenReturn(List.of(job));
            when(applicationRepository.countByJobId(job.getId())).thenReturn(5L);

            List<RecruiterDashboardResponse> dashboard = jobService.getRecruiterDashboard(recruiter.getId());

            assertThat(dashboard).hasSize(1);
            assertThat(dashboard.get(0).getJobTitle()).isEqualTo("Senior Java Developer");
            assertThat(dashboard.get(0).getApplicantCount()).isEqualTo(5L);
        }
    }
}

package com.jobportal.service;

import com.jobportal.dto.response.ApplicationResponse;
import com.jobportal.entity.*;
import com.jobportal.exception.DuplicateApplicationException;
import com.jobportal.exception.InvalidStatusTransitionException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.exception.UnauthorizedActionException;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.CandidateProfileRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationServiceImpl Unit Tests")
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CandidateProfileRepository candidateProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private User candidate;
    private User recruiter;
    private Job job;
    private Application application;

    @BeforeEach
    void setUp() {
        // Set the uploadDir field via reflection since it's injected via @Value
        ReflectionTestUtils.setField(applicationService, "uploadDir", "./uploads/test-resumes");

        candidate = User.builder()
                .id(UUID.randomUUID())
                .name("Jane Candidate")
                .email("candidate@test.com")
                .password("encoded")
                .role(UserRole.CANDIDATE)
                .createdAt(LocalDateTime.now())
                .build();

        recruiter = User.builder()
                .id(UUID.randomUUID())
                .name("John Recruiter")
                .email("recruiter@test.com")
                .password("encoded")
                .role(UserRole.RECRUITER)
                .createdAt(LocalDateTime.now())
                .build();

        job = Job.builder()
                .id(UUID.randomUUID())
                .title("Senior Java Developer")
                .description("Job description")
                .requiredSkills("Java, Spring Boot")
                .location("Bangalore")
                .experienceRequired(5)
                .salaryRange("15-25 LPA")
                .postedBy(recruiter)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        application = Application.builder()
                .id(UUID.randomUUID())
                .job(job)
                .candidate(candidate)
                .resumeUrl("/uploads/resume.pdf")
                .status(ApplicationStatus.APPLIED)
                .appliedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("applyToJob")
    class ApplyToJobTests {

        @Test
        @DisplayName("should apply to a job successfully without resume")
        void shouldApplySuccessfully() {
            when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
            when(userRepository.findById(candidate.getId())).thenReturn(Optional.of(candidate));
            when(applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidate.getId())).thenReturn(false);
            when(applicationRepository.save(any(Application.class))).thenReturn(application);

            ApplicationResponse response = applicationService.applyToJob(job.getId(), candidate.getId(), null);

            assertThat(response).isNotNull();
            assertThat(response.getJobTitle()).isEqualTo("Senior Java Developer");
            assertThat(response.getCandidateName()).isEqualTo("Jane Candidate");
            assertThat(response.getStatus()).isEqualTo("APPLIED");
            verify(applicationRepository).save(any(Application.class));
        }

        @Test
        @DisplayName("should throw DuplicateApplicationException when applying twice to the same job")
        void shouldThrowOnDuplicateApplication() {
            when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
            when(userRepository.findById(candidate.getId())).thenReturn(Optional.of(candidate));
            when(applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidate.getId())).thenReturn(true);

            assertThatThrownBy(() -> applicationService.applyToJob(job.getId(), candidate.getId(), null))
                    .isInstanceOf(DuplicateApplicationException.class)
                    .hasMessageContaining("already applied");

            verify(applicationRepository, never()).save(any(Application.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when job does not exist")
        void shouldThrowWhenJobNotFound() {
            UUID unknownJobId = UUID.randomUUID();
            when(jobRepository.findById(unknownJobId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.applyToJob(unknownJobId, candidate.getId(), null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when applying to inactive job")
        void shouldThrowWhenJobInactive() {
            job.setIsActive(false);
            when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

            assertThatThrownBy(() -> applicationService.applyToJob(job.getId(), candidate.getId(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("inactive");
        }
    }

    @Nested
    @DisplayName("updateApplicationStatus")
    class UpdateStatusTests {

        @Test
        @DisplayName("should transition from APPLIED to SHORTLISTED")
        void shouldTransitionAppliedToShortlisted() {
            application.setStatus(ApplicationStatus.APPLIED);
            when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
            when(applicationRepository.save(any(Application.class))).thenReturn(application);
            when(candidateProfileRepository.findByUser_Id(candidate.getId())).thenReturn(Optional.empty());

            ApplicationResponse response = applicationService.updateApplicationStatus(
                    application.getId(), "SHORTLISTED", recruiter.getId());

            assertThat(response.getStatus()).isEqualTo("SHORTLISTED");
        }

        @Test
        @DisplayName("should transition from SHORTLISTED to INTERVIEW")
        void shouldTransitionShortlistedToInterview() {
            application.setStatus(ApplicationStatus.SHORTLISTED);
            when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
            when(applicationRepository.save(any(Application.class))).thenReturn(application);
            when(candidateProfileRepository.findByUser_Id(candidate.getId())).thenReturn(Optional.empty());

            ApplicationResponse response = applicationService.updateApplicationStatus(
                    application.getId(), "INTERVIEW", recruiter.getId());

            assertThat(response.getStatus()).isEqualTo("INTERVIEW");
        }

        @Test
        @DisplayName("should transition from INTERVIEW to HIRED")
        void shouldTransitionInterviewToHired() {
            application.setStatus(ApplicationStatus.INTERVIEW);
            when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
            when(applicationRepository.save(any(Application.class))).thenReturn(application);
            when(candidateProfileRepository.findByUser_Id(candidate.getId())).thenReturn(Optional.empty());

            ApplicationResponse response = applicationService.updateApplicationStatus(
                    application.getId(), "HIRED", recruiter.getId());

            assertThat(response.getStatus()).isEqualTo("HIRED");
        }

        @Test
        @DisplayName("should throw InvalidStatusTransitionException for REJECTED → APPLIED")
        void shouldThrowOnInvalidTransitionFromRejected() {
            application.setStatus(ApplicationStatus.REJECTED);
            when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                    application.getId(), "APPLIED", recruiter.getId()))
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("should throw InvalidStatusTransitionException for HIRED → REJECTED")
        void shouldThrowOnInvalidTransitionFromHired() {
            application.setStatus(ApplicationStatus.HIRED);
            when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                    application.getId(), "REJECTED", recruiter.getId()))
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("should throw InvalidStatusTransitionException for APPLIED → HIRED (skipping steps)")
        void shouldThrowWhenSkippingSteps() {
            application.setStatus(ApplicationStatus.APPLIED);
            when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                    application.getId(), "HIRED", recruiter.getId()))
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("should throw UnauthorizedActionException when different recruiter tries to update")
        void shouldThrowWhenNotJobOwner() {
            User otherRecruiter = User.builder()
                    .id(UUID.randomUUID())
                    .name("Other")
                    .email("other@test.com")
                    .role(UserRole.RECRUITER)
                    .build();

            when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                    application.getId(), "SHORTLISTED", otherRecruiter.getId()))
                    .isInstanceOf(UnauthorizedActionException.class);
        }
    }

    @Nested
    @DisplayName("getCandidateApplications")
    class CandidateDashboardTests {

        @Test
        @DisplayName("should return all applications for a candidate")
        void shouldReturnCandidateApplications() {
            when(applicationRepository.findByCandidateIdOrderByAppliedAtDesc(candidate.getId()))
                    .thenReturn(List.of(application));

            List<ApplicationResponse> responses = applicationService.getCandidateApplications(candidate.getId());

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getCandidateName()).isEqualTo("Jane Candidate");
            assertThat(responses.get(0).getJobTitle()).isEqualTo("Senior Java Developer");
        }

        @Test
        @DisplayName("should return empty list when candidate has no applications")
        void shouldReturnEmptyList() {
            when(applicationRepository.findByCandidateIdOrderByAppliedAtDesc(candidate.getId()))
                    .thenReturn(List.of());

            List<ApplicationResponse> responses = applicationService.getCandidateApplications(candidate.getId());

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("getJobApplications")
    class JobApplicationsTests {

        @Test
        @DisplayName("should return applications for a recruiter's job")
        void shouldReturnJobApplications() {
            when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
            when(applicationRepository.findByJobIdOrderByAppliedAtDesc(job.getId()))
                    .thenReturn(List.of(application));
            when(candidateProfileRepository.findByUser_Id(candidate.getId())).thenReturn(Optional.empty());

            List<ApplicationResponse> responses = applicationService.getJobApplications(
                    job.getId(), recruiter.getId());

            assertThat(responses).hasSize(1);
        }

        @Test
        @DisplayName("should throw UnauthorizedActionException for non-owner recruiter")
        void shouldThrowForNonOwner() {
            UUID otherRecruiterId = UUID.randomUUID();
            when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

            assertThatThrownBy(() -> applicationService.getJobApplications(job.getId(), otherRecruiterId))
                    .isInstanceOf(UnauthorizedActionException.class);
        }
    }
}

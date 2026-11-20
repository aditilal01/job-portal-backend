package com.jobportal.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.dto.response.AiJobMatchResponse;
import com.jobportal.dto.response.AiResumeAnalysisResponse;
import com.jobportal.entity.CandidateResume;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.entity.UserRole;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.CandidateResumeRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.CandidateAiService;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class CandidateAiServiceImpl implements CandidateAiService {
    private static final Logger log = LoggerFactory.getLogger(CandidateAiServiceImpl.class);
    private static final long MAX_RESUME_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword");

    private final CandidateResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final Tika tika = new Tika();
    private final String uploadDir;
    private final String apiKey;
    private final String model;

    public CandidateAiServiceImpl(CandidateResumeRepository resumeRepository, UserRepository userRepository,
            JobRepository jobRepository, ObjectMapper objectMapper,
            @Value("${app.file.upload-dir}") String uploadDir,
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.model:gpt-4o-mini}") String model) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
        this.uploadDir = uploadDir;
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
    }

    @Override
    @Transactional
    public AiResumeAnalysisResponse analyzeResume(UUID candidateId, MultipartFile file) {
        User candidate = getCandidate(candidateId);
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Please select a resume file.");
        if (file.getSize() > MAX_RESUME_BYTES) throw new IllegalArgumentException("Resume must not exceed 10 MB.");
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("resume").replaceAll("[\\\\/\\\\r\\\\n]", "_");
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!Set.of("pdf", "doc", "docx").contains(extension)) {
            throw new IllegalArgumentException("Upload a PDF, DOC, or DOCX resume.");
        }

        String extracted;
        try (InputStream input = file.getInputStream()) {
            String detected = tika.detect(input, originalName);
            boolean supported = "application/pdf".equals(detected)
                    || detected.contains("wordprocessingml") || "application/msword".equals(detected)
                    || "application/x-tika-msoffice".equals(detected);
            if (!supported) throw new IllegalArgumentException("The uploaded file does not appear to be a supported resume document.");
            try (InputStream parseInput = file.getInputStream()) {
                extracted = tika.parseToString(parseInput);
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Unable to extract text from uploaded resume", ex);
            throw new IllegalArgumentException("We could not read this resume. Please upload a text-based PDF, DOC, or DOCX file.");
        }
        if (extracted == null || extracted.isBlank()) throw new IllegalArgumentException("No readable text was found in this resume. Please upload a text-based document.");
        if (extracted.length() > 30000) extracted = extracted.substring(0, 30000);

        String storedName = UUID.randomUUID() + "." + extension;
        try {
            Path root = Paths.get(uploadDir, "candidate-ai").toAbsolutePath().normalize();
            Files.createDirectories(root);
            Path target = root.resolve(storedName).normalize();
            if (!target.startsWith(root)) throw new IllegalArgumentException("Invalid resume filename.");
            try (InputStream storedInput = file.getInputStream()) {
                Files.copy(storedInput, target);
            }
        } catch (Exception ex) {
            log.error("Could not store candidate AI resume", ex);
            throw new IllegalStateException("Resume upload could not be saved.");
        }

        CandidateResume resume = resumeRepository.findByUser_Id(candidateId)
                .orElseGet(() -> CandidateResume.builder().user(candidate).build());
        String oldStored = resume.getStoredFilename();
        resume.setOriginalFilename(originalName);
        resume.setStoredFilename(storedName);
        resume.setExtractedText(extracted);
        resumeRepository.save(resume);
        if (oldStored != null && !oldStored.equals(storedName)) {
            try { Files.deleteIfExists(Paths.get(uploadDir, "candidate-ai", oldStored)); }
            catch (Exception ex) { log.warn("Could not remove replaced candidate resume file", ex); }
        }

        JsonNode result = askModel(
                "Analyze the resume text. Return ONLY a JSON object with keys professionalSummary (string), skills (array of strings), strengths (array of strings), improvementAreas (array of strings), experienceLevel (string). Do not invent qualifications; distinguish evidence from suggestions.",
                extracted);
        return AiResumeAnalysisResponse.builder()
                .resumeFilename(originalName)
                .professionalSummary(text(result, "professionalSummary"))
                .skills(strings(result, "skills"))
                .strengths(strings(result, "strengths"))
                .improvementAreas(strings(result, "improvementAreas"))
                .experienceLevel(text(result, "experienceLevel"))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AiJobMatchResponse getJobMatches(UUID candidateId) {
        getCandidate(candidateId);
        CandidateResume resume = resumeRepository.findByUser_Id(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate resume", "userId", candidateId));
        List<Job> jobs = jobRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        if (jobs.isEmpty()) return AiJobMatchResponse.builder().matches(List.of()).build();

        StringBuilder promptData = new StringBuilder("CANDIDATE RESUME:\\n")
                .append(resume.getExtractedText()).append("\\n\\nACTIVE JOBS:\\n");
        for (Job job : jobs.stream().limit(20).toList()) {
            promptData.append("JOB_ID: ").append(job.getId()).append("\\nTITLE: ").append(job.getTitle())
                    .append("\\nDESCRIPTION: ").append(job.getDescription().substring(0, Math.min(job.getDescription().length(), 2500)))
                    .append("\\nREQUIRED SKILLS: ").append(job.getRequiredSkills())
                    .append("\\nEXPERIENCE REQUIRED: ").append(job.getExperienceRequired())
                    .append("\\n---\\n");
        }
        JsonNode result = askModel(
                "Compare the candidate resume to each supplied job. Return ONLY JSON: {matches:[{jobId:string,matchScore:integer 0-100,matchingSkills:string[],missingSkills:string[],explanation:string}]}. Include only supplied job IDs, rank best first, score based on demonstrated evidence, never infer missing qualifications. This is advisory decision support, not a hiring decision.",
                promptData.toString());
        List<AiJobMatchResponse.JobMatch> matches = new ArrayList<>();
        JsonNode items = result.path("matches");
        if (items.isArray()) for (JsonNode item : items) {
            try {
                UUID id = UUID.fromString(item.path("jobId").asText());
                Job job = jobs.stream().filter(j -> j.getId().equals(id)).findFirst().orElse(null);
                if (job == null) continue;
                int score = Math.max(0, Math.min(100, item.path("matchScore").asInt()));
                matches.add(AiJobMatchResponse.JobMatch.builder().jobId(id).jobTitle(job.getTitle())
                        .matchScore(score).matchingSkills(strings(item, "matchingSkills"))
                        .missingSkills(strings(item, "missingSkills")).explanation(text(item, "explanation")).build());
            } catch (Exception ex) { log.debug("Ignoring malformed AI job match item", ex); }
        }
        matches.sort(Comparator.comparingInt(AiJobMatchResponse.JobMatch::getMatchScore).reversed());
        return AiJobMatchResponse.builder().matches(matches).build();
    }

    private User getCandidate(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        if (user.getRole() != UserRole.CANDIDATE) throw new IllegalArgumentException("Only candidate accounts can use AI career tools.");
        return user;
    }

    private JsonNode askModel(String instruction, String input) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI features are not configured yet. Add OPENAI_API_KEY to the backend environment and restart it.");
        }
        try {
            Map<String, Object> request = Map.of(
                    "model", model,
                    "temperature", 0.2,
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(
                            Map.of("role", "system", "content", instruction),
                            Map.of("role", "user", "content", input)));
            String response = restClient.post().uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request).retrieve().body(String.class);
            JsonNode envelope = objectMapper.readTree(response);
            String content = envelope.path("choices").path(0).path("message").path("content").asText();
            if (content.isBlank()) throw new IllegalStateException("The AI provider returned an empty response.");
            return objectMapper.readTree(content);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("AI provider request failed", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The AI service could not complete the request. Please try again shortly.");
        }
    }

    private String text(JsonNode node, String key) { return node.path(key).asText(""); }
    private List<String> strings(JsonNode node, String key) {
        List<String> values = new ArrayList<>();
        JsonNode array = node.path(key);
        if (array.isArray()) array.forEach(value -> { if (value.isTextual() && !value.asText().isBlank()) values.add(value.asText()); });
        return values;
    }
}

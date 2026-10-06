package com.example.employeeapp.controller;

import com.example.employeeapp.entity.CandidateApplication;
import com.example.employeeapp.entity.Project;
import com.example.employeeapp.repository.CandidateApplicationRepository;
import com.example.employeeapp.repository.ProjectRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIChatController {
    @Value("${gemini.api.key:}") private String geminiApiKey;
    @Value("${gemini.model:gemini-3.5-flash}") private String geminiModel;

    private final CandidateApplicationRepository applications;
    private final ProjectRepository projects;
    private final ObjectMapper mapper;
    private final HttpClient http = HttpClient.newHttpClient();

    public AIChatController(CandidateApplicationRepository applications, ProjectRepository projects, ObjectMapper mapper) {
        this.applications = applications; this.projects = projects; this.mapper = mapper;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        if (request.message == null || request.message.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Please enter a question."));
        if (geminiApiKey == null || geminiApiKey.isBlank())
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", "PeoplePulse AI is not configured. Set GEMINI_API_KEY and restart the backend."));
        try {
            String context = buildContext(request);
            String system = """
                    You are PeoplePulse AI, the assistant inside an employee recruitment application.
                    Answer from the supplied PeoplePulse context plus general explanations of how the application works.
                    Be concise, practical and clear.
                    A CANDIDATE may only receive information about their own applications and public jobs.
                    MANAGER and ADMIN may receive recruitment/project information included in their context.
                    Never reveal passwords, API keys, resume binary data, or private information outside the user's permitted context.
                    You are informational only: never claim that you approved, rejected, selected, edited, created, or deleted anything.
                    If asked to perform an action, explain where to do it in the PeoplePulse UI.
                    If the context does not contain the answer, say you do not have enough PeoplePulse data.

                    PEOPLEPULSE CONTEXT:
                    """ + context;

            Map<String,Object> body = new LinkedHashMap<>();
            body.put("system_instruction", Map.of("parts", List.of(Map.of("text", system))));
            body.put("contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", request.message.trim())))));
            String requestBody = mapper.writeValueAsString(body);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent";
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", geminiApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String detail = response.body();
                if (detail == null || detail.isBlank()) detail = "Unknown Gemini API error.";
                if (detail.length() > 1000) detail = detail.substring(0, 1000);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", "Gemini AI service error: " + detail));
            }
            String answer = extractGeminiText(mapper.readTree(response.body()));
            return ResponseEntity.ok(Map.of("answer", answer.isBlank() ? "I received an empty response from Gemini." : answer));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", "Unable to contact PeoplePulse AI: " + e.getMessage()));
        }
    }

    private String buildContext(ChatRequest r) {
        String role = r.role == null ? "UNKNOWN" : r.role.toUpperCase(Locale.ROOT);
        StringBuilder c = new StringBuilder("User: ").append(r.name == null ? r.username : r.name)
                .append(" | username: ").append(r.username).append(" | role: ").append(role).append("\n\n");
        c.append("AVAILABLE JOBS / PROJECTS:\n");
        for (Project p : projects.findAll()) {
            c.append("- ").append(p.getName()).append(" | role=").append(Optional.ofNullable(p.getJobRole()).orElse("not configured"))
                    .append(" | location=").append(Optional.ofNullable(p.getLocation()).orElse("not configured"))
                    .append(" | experience=").append(Optional.ofNullable(p.getExperience()).orElse("not configured"))
                    .append(" | budget=").append(p.getBudget()).append(" | required skills=").append(Optional.ofNullable(p.getSkills()).orElse("not configured"))
                    .append(" | description=").append(Optional.ofNullable(p.getJobDescription()).orElse("not configured")).append("\n");
        }
        List<CandidateApplication> list = "CANDIDATE".equals(role)
                ? applications.findByCandidateUsernameIgnoreCaseOrderByAppliedAtDesc(r.username == null ? "" : r.username)
                : applications.findAll();
        c.append("\nAPPLICATIONS:\n");
        for (CandidateApplication a : list.stream().limit(100).collect(Collectors.toList())) {
            c.append("- ").append(a.getApplicationCode()).append(" | candidate=").append(a.getCandidateName())
                    .append(" | job=").append(Optional.ofNullable(a.getProjectName()).orElse(a.getAppliedRole()))
                    .append(" | status=").append(a.getStatus()).append(" | appliedAt=").append(a.getAppliedAt());
            if ("CANDIDATE".equals(role)) {
                if (a.getRejectionComment()!=null && !a.getRejectionComment().isBlank()) c.append(" | rejectionReason=").append(a.getRejectionComment());
                if (a.getManagerComment()!=null && !a.getManagerComment().isBlank()) c.append(" | managerComment=").append(a.getManagerComment());
            }
            c.append("\n");
        }
        if (list.isEmpty()) c.append("- No applications found for this user.\n");
        return c.toString();
    }

    private String extractGeminiText(JsonNode root) {
        List<String> parts = new ArrayList<>();
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray()) for (JsonNode candidate : candidates) {
            JsonNode responseParts = candidate.path("content").path("parts");
            if (responseParts.isArray()) for (JsonNode part : responseParts) {
                JsonNode text = part.get("text");
                if (text != null && text.isTextual()) parts.add(text.asText());
            }
        }
        return String.join("\n", parts).trim();
    }

    public static class ChatRequest { public String username; public String role; public String name; public String message; }
}

package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.MemberRepository;
import com.example.GymManagementSystem.repository.TrainerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final String SYSTEM_PROMPT = """
            You are FitLife AI, a helpful and knowledgeable gym and fitness assistant.

            Your primary purpose is to help users with gym training, workouts, exercises,
            fitness, general nutrition, healthy fitness habits, and the FitLife Gym
            Management System. Understand natural language and answer in the same language
            as the user when possible, including natural Hindi/Hinglish.

            Give practical, accurate, beginner-friendly answers. Keep answers focused and
            avoid unnecessary length. Use headings or bullets when useful. Explain technical
            concepts simply and be encouraging but professional.

            For FitLife membership questions, use only the verified account context supplied
            by the application. Never invent personal account information and never reveal
            information about another user.

            Provide general fitness and nutrition information only. Do not diagnose medical
            conditions or injuries, prescribe medication, or provide dangerous advice.
            Encourage the user to consult a qualified doctor, physiotherapist, or certified
            healthcare professional for medical conditions, injuries, serious symptoms,
            prescription medications, pregnancy-related concerns, or unsafe requests.

            If a question is unrelated to fitness or the FitLife Gym Management System,
            politely say: "I'm FitLife AI, your gym and fitness assistant. I can help you
            with workouts, exercises, fitness, training, and general nutrition questions."
            Do not answer unrelated questions.
            """;

    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;

    public AiService(
            MemberRepository memberRepository,
            TrainerRepository trainerRepository,
            ObjectMapper objectMapper,
            @Value("${ai.gemini.api-key:}") String apiKey,
            @Value("${ai.gemini.model:gemini-3.1-flash-lite}") String model) {
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String chat(User user, String message) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI assistant is not configured. Set GEMINI_API_KEY on the backend.");
        }

        try {
            String combinedInstruction = SYSTEM_PROMPT + "\n\n" + accountContext(user);
            Map<String, Object> payload = Map.of(
                    "systemInstruction", Map.of(
                            "parts", List.of(Map.of("text", combinedInstruction))
                    ),
                    "contents", List.of(Map.of(
                            "role", "user",
                            "parts", List.of(Map.of("text", message.trim()))
                    )),
                    "generationConfig", Map.of(
                            "temperature", 0.4,
                            "maxOutputTokens", 700
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/"
                            + URLEncoder.encode(model, StandardCharsets.UTF_8)
                            + ":generateContent"))
                    .timeout(Duration.ofSeconds(45))
                    .header("x-goog-api-key", apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = GeminiRequestExecutor.send(httpClient, request);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String reason = switch (response.statusCode()) {
                    case 400 -> "Gemini rejected the request. Check the backend API key and model configuration.";
                    case 401, 403 -> "Gemini denied access. Check GEMINI_API_KEY and its API permissions.";
                    case 404 -> "The configured Gemini model is unavailable. Check GEMINI_MODEL on the backend.";
                    case 429 -> "Gemini quota or rate limit reached. Check your API quota and billing, then retry.";
                    case 503 -> "Gemini is experiencing high demand. Please retry shortly.";
                    default -> "Gemini could not complete the request.";
                };
                throw new IllegalStateException(reason + " (HTTP " + response.statusCode() + ")");
            }
            JsonNode body = objectMapper.readTree(response.body());
            StringBuilder answerBuilder = new StringBuilder();
            for (JsonNode part : body.path("candidates").path(0).path("content").path("parts")) {
                if (part.hasNonNull("text") && !part.path("thought").asBoolean(false)) {
                    answerBuilder.append(part.path("text").asText());
                }
            }
            String answer = answerBuilder.toString();
            if (answer.isBlank()) {
                throw new IllegalStateException("The AI provider returned an empty response.");
            }
            return answer.trim();
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("The AI request was interrupted. Please try again.", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("The AI assistant is temporarily unavailable. Please try again.", exception);
        }
    }

    private String accountContext(User user) {
        List<String> lines = new ArrayList<>();
        lines.add("Verified FitLife account context for the authenticated user only:");
        lines.add("- Role: " + safe(user.getRole()));
        lines.add("- Display name: " + safe(user.getDisplayName()));

        if ("MEMBER".equalsIgnoreCase(user.getRole())) {
            Member member = memberRepository.findByUserId(user.getId());
            if (member != null) {
                lines.add("- Membership plan: " + safe(member.getMembershipType()));
                lines.add("- Membership start date: " + safe(member.getMembershipStartDate()));
                lines.add("- Membership expiry date: " + safe(member.getMembershipEndDate()));
                lines.add("- Membership status: " + membershipStatus(member.getMembershipEndDate()));
                lines.add("- Assigned trainer: " + safe(member.getTrainerName()));
            } else {
                lines.add("- No member profile is currently linked to this account.");
            }
        } else if ("TRAINER".equalsIgnoreCase(user.getRole())) {
            Trainer trainer = trainerRepository.findByUser_Id(user.getId());
            if (trainer != null) {
                lines.add("- Trainer specialty: " + safe(trainer.getSpecialty()));
            }
        }
        lines.add("Do not disclose this context unless it directly answers the user's own account question.");
        return String.join("\n", lines);
    }

    private String membershipStatus(String endDate) {
        if (endDate == null || endDate.isBlank()) return "not available";
        try {
            return LocalDate.parse(endDate).isBefore(LocalDate.now()) ? "expired" : "active";
        } catch (Exception ignored) {
            return "not available";
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "not available" : value;
    }
}

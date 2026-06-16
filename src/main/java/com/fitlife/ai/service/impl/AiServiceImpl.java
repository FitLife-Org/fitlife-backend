package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fitlife.ai.dto.AiWorkoutRequest;
import com.fitlife.ai.entity.AiWorkoutPlan;
import com.fitlife.ai.repository.AiWorkoutPlanRepository;
import com.fitlife.ai.service.AiService;
import com.fitlife.member.entity.BodyMetric;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.BodyMetricRepository;
import com.fitlife.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class
AiServiceImpl implements AiService {

    private final MemberRepository memberRepository;
    private final BodyMetricRepository bodyMetricRepository;
    private final AiWorkoutPlanRepository aiWorkoutPlanRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.api-url}")
    private String geminiApiUrl;

    @Transactional
    @Override
    public JsonNode generateWorkoutPlan(String username, AiWorkoutRequest request) {
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        BodyMetric latestMetric = bodyMetricRepository.findFirstByMemberOrderByRecordedAtDesc(member)
                .orElseThrow(() -> new RuntimeException("Member does not have body metrics yet"));

        String prompt = buildPrompt(member, latestMetric, request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(createGeminiPayload(prompt), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(geminiApiUrl + "?key=" + geminiApiKey, entity, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String aiResponseText = rootNode.at("/candidates/0/content/parts/0/text").asText();
            String cleanJson = extractJson(aiResponseText);

            AiWorkoutPlan savedPlan = aiWorkoutPlanRepository.save(AiWorkoutPlan.builder()
                    .member(member)
                    .goal(request.getGoal())
                    .level(request.getFitnessLevel())
                    .durationWeeks(4)
                    .planSummary(cleanJson)
                    .status("ACTIVE")
                    .generatedBy("GEMINI")
                    .build());

            JsonNode resultNode = objectMapper.readTree(cleanJson);
            if (resultNode.isObject()) {
                ((ObjectNode) resultNode).put("planId", savedPlan.getId());
            }
            return resultNode;
        } catch (Exception e) {
            log.error("AI service error", e);
            throw new RuntimeException("AI service is unavailable or returned invalid data");
        }
    }

    @Override
    public List<AiWorkoutPlan> getMemberHistory(String username) {
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return aiWorkoutPlanRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    private String extractJson(String text) {
        String clean = text.trim();
        if (clean.contains("```json")) {
            clean = clean.substring(clean.indexOf("```json") + 7);
            clean = clean.substring(0, clean.lastIndexOf("```"));
        } else if (clean.contains("```")) {
            clean = clean.substring(clean.indexOf("```") + 3);
            clean = clean.substring(0, clean.lastIndexOf("```"));
        }
        return clean.trim();
    }

    private Map<String, Object> createGeminiPayload(String prompt) {
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));
        Map<String, Object> config = new HashMap<>();
        config.put("temperature", 0.3);
        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", List.of(content));
        payload.put("generationConfig", config);
        return payload;
    }

    private String buildPrompt(Member member, BodyMetric metric, AiWorkoutRequest req) {
        return String.format(
                "Create a personalized workout plan as raw JSON only for member %s. Weight %.1f kg, height %.1f cm, BMI %.1f. Goal: %s. Injuries: %s. Fitness level: %s. Equipment: %s. Include disclaimer, advice, nutritionPlan, and workoutSchedule items with day, focus, exercises name, sets, reps, notes.",
                member.getFullName(), metric.getWeight(), metric.getHeight(), metric.getBmi(),
                req.getGoal(), blankToDefault(req.getInjuries(), "none"),
                req.getFitnessLevel(), blankToDefault(req.getEquipment(), "standard gym equipment")
        );
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
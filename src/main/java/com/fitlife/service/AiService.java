package com.fitlife.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.dto.AiWorkoutRequest;
import com.fitlife.entity.*;
import com.fitlife.repository.*;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final MemberRepository memberRepository;
    private final HealthMetricRepository healthMetricRepository;
    private final AiPlanRepository aiPlanRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.api-url}")
    private String geminiApiUrl;

    /**
     * Giai đoạn 1: Gọi AI tạo phác đồ và lưu vào lịch sử (JSON)
     */
    @Transactional
    public JsonNode generateWorkoutPlan(AiWorkoutRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên"));

        HealthMetric latestMetric = healthMetricRepository.findFirstByMemberOrderByRecordedDateDesc(member)
                .orElseThrow(() -> new RuntimeException("Hội viên chưa có chỉ số BMI. Hãy đo trước khi tư vấn AI."));

        String prompt = buildPrompt(member, latestMetric, request);

        // Payload chuẩn hóa cho v1beta
        Map<String, Object> requestBody = createGeminiPayload(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

        try {
            log.info("===> Đang gửi yêu cầu tới Gemini 2.5 Flash cho: {}", member.getFullName());
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String aiResponseText = rootNode.at("/candidates/0/content/parts/0/text").asText();

            // Làm sạch JSON (Xử lý trường hợp AI trả về Markdown)
            String cleanJson = extractJson(aiResponseText);

            // Lưu vào bảng lịch sử AI
            AiWorkoutPlan planHistory = AiWorkoutPlan.builder()
                    .member(member)
                    .goal(request.getGoal())
                    .planData(cleanJson)
                    .createdAt(LocalDateTime.now())
                    .build();
            aiPlanRepository.save(planHistory);

            log.info("===> Đã lưu phác đồ AI vào lịch sử.");
            return objectMapper.readTree(cleanJson);

        } catch (Exception e) {
            log.error("Lỗi AI Service: {}", e.getMessage());
            throw new RuntimeException("Hệ thống AI đang bận hoặc gặp sự cố định dạng.");
        }
    }

    /**
     * Giai đoạn 2: Bóc tách JSON để áp dụng vào lịch tập chính thức
     */
    @Transactional
    public void activatePlan(Long aiPlanId) {
        AiWorkoutPlan aiPlanRecord = aiPlanRepository.findById(aiPlanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phác đồ AI trong lịch sử"));

        Member member = aiPlanRecord.getMember();

        try {
            JsonNode root = objectMapper.readTree(aiPlanRecord.getPlanData());

            // 1. Hủy các lịch tập ACTIVE cũ
            workoutPlanRepository.findByMemberAndStatus(member, WorkoutPlan.PlanStatus.ACTIVE)
                    .ifPresent(p -> {
                        p.setStatus(WorkoutPlan.PlanStatus.CANCELLED);
                        workoutPlanRepository.save(p);
                    });

            // 2. Tạo WorkoutPlan chính thức
            WorkoutPlan officialPlan = WorkoutPlan.builder()
                    .name("Lịch tập AI: " + aiPlanRecord.getGoal())
                    .description(root.path("advice").asText())
                    .member(member)
                    .startDate(LocalDateTime.now())
                    .status(WorkoutPlan.PlanStatus.ACTIVE)
                    .build();

            List<WorkoutSession> sessions = new ArrayList<>();
            JsonNode scheduleNode = root.path("workoutSchedule");

            for (JsonNode dayNode : scheduleNode) {
                // 3. Tạo Session cho từng ngày tập
                WorkoutSession session = WorkoutSession.builder()
                        .dayOfWeek(dayNode.path("day").asText())
                        .focusArea(dayNode.path("focus").asText())
                        .workoutPlan(officialPlan)
                        .build();

                List<WorkoutDetail> details = new ArrayList<>();
                JsonNode exercisesNode = dayNode.path("exercises");

                for (JsonNode exNode : exercisesNode) {
                    // 4. Tạo chi tiết bài tập từ JSON
                    WorkoutDetail detail = WorkoutDetail.builder()
                            .exercise_name(exNode.path("name").asText())
                            .sets(exNode.path("sets").asInt())
                            .reps(exNode.path("reps").asText())
                            .notes(exNode.path("notes").asText())
                            .session(session)
                            .build();
                    details.add(detail);
                }
                session.setDetails(details);
                sessions.add(session);
            }

            officialPlan.setSessions(sessions);
            workoutPlanRepository.save(officialPlan);
            log.info("===> Lịch tập chính thức đã được kích hoạt cho: {}", member.getFullName());

        } catch (Exception e) {
            log.error("Lỗi Kích hoạt: {}", e.getMessage());
            throw new RuntimeException("Cấu trúc phác đồ không tương thích để kích hoạt.");
        }
    }

    public List<AiWorkoutPlan> getMemberHistory(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên"));
        return aiPlanRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    // --- HELPER METHODS ---

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
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));
        payload.put("contents", List.of(content));

        Map<String, Object> config = new HashMap<>();
        config.put("temperature", 0.3); // Giữ sự sáng tạo ở mức an toàn
        payload.put("generationConfig", config);
        return payload;
    }

    private String buildPrompt(Member member, HealthMetric metric, AiWorkoutRequest req) {
        return String.format(
                "Bạn là Chuyên gia thể hình chuẩn NASM. Hãy tạo lộ trình cho: %s. " +
                        "Thông số: Nặng %.1fkg, Cao %.1fcm, BMI %.1f. Mục tiêu: %s. " +
                        "Chấn thương: %s. Trình độ: %s. Thiết bị: %s. " +
                        "YÊU CẦU: Trả về DUY NHẤT 1 chuỗi JSON thuần túy (không giải thích), cấu trúc y hệt: " +
                        "{\"disclaimer\": \"...\", \"advice\": \"...\", \"nutritionPlan\": {\"targetCalories\": 2000}, " +
                        "\"workoutSchedule\": [{\"day\": \"Thứ...\", \"focus\": \"...\", \"exercises\": [{\"name\": \"...\", \"sets\": 3, \"reps\": \"12\", \"notes\": \"...\"}]}]}",
                member.getFullName(), metric.getWeight(), metric.getHeight(), metric.getBmi(),
                req.getGoal(), (req.getInjuries() != null ? req.getInjuries() : "Không"),
                req.getFitnessLevel(), (req.getEquipment() != null ? req.getEquipment() : "Full Gym")
        );
    }
}
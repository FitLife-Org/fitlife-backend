package com.fitlife.ai_workout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fitlife.ai_workout.dto.AiWorkoutRequest;
import com.fitlife.ai_workout.entity.AiWorkoutPlan;
import com.fitlife.ai_workout.entity.WorkoutDetail;
import com.fitlife.ai_workout.entity.WorkoutPlan;
import com.fitlife.ai_workout.entity.WorkoutSession;
import com.fitlife.member.entity.HealthMetric;
import com.fitlife.member.entity.Member;
import com.fitlife.member.HealthMetricRepository;
import com.fitlife.member.MemberRepository;
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
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final MemberRepository memberRepository;
    private final HealthMetricRepository healthMetricRepository;
    private final AiWorkoutPlanRepository aiWorkoutPlanRepository;
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
    @Override
    public JsonNode generateWorkoutPlan(String username, AiWorkoutRequest request) {

        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên"));

        HealthMetric latestMetric = healthMetricRepository.findFirstByMemberOrderByRecordedAtDesc(member)
                .orElseThrow(() -> new RuntimeException("Hội viên chưa có chỉ số sức khỏe. Vui lòng cập nhật chiều cao/cân nặng tại Dashboard."));

        String prompt = buildPrompt(member, latestMetric, request);

        Map<String, Object> requestBody = createGeminiPayload(prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

        try {
            log.info("===> Đang gửi yêu cầu tới Gemini cho: {}", member.getFullName());
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String aiResponseText = rootNode.at("/candidates/0/content/parts/0/text").asText();

            String cleanJson = extractJson(aiResponseText);

            AiWorkoutPlan planHistory = AiWorkoutPlan.builder()
                    .member(member)
                    .goal(request.getGoal())
                    .planData(cleanJson)
                    .createdAt(LocalDateTime.now())
                    .build();

            planHistory = aiWorkoutPlanRepository.save(planHistory);

            log.info("===> Đã lưu phác đồ AI vào lịch sử thành công.");

            JsonNode resultNode = objectMapper.readTree(cleanJson);
            if (resultNode.isObject()) {
                ((ObjectNode) resultNode).put("planId", planHistory.getId());
            }

            return resultNode;

        } catch (Exception e) {
            log.error("Lỗi AI Service: {}", e.getMessage());
            throw new RuntimeException("Hệ thống AI đang bận hoặc phản hồi không đúng định dạng. Vui lòng thử lại sau.");
        }
    }

    /**
     * Giai đoạn 2: Bóc tách JSON từ lịch sử và áp dụng vào bảng WorkoutPlan chính thức.
     */
    @Transactional
    @Override
    public void activatePlan(Long aiPlanId) {
        AiWorkoutPlan aiPlanRecord = aiWorkoutPlanRepository.findById(aiPlanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phác đồ AI trong lịch sử"));

        Member member = aiPlanRecord.getMember();

        try {
            JsonNode root = objectMapper.readTree(aiPlanRecord.getPlanData());

            // FIX 1: Đổi Enum thành chuỗi "ACTIVE"
            workoutPlanRepository.findByMemberAndStatus(member, "ACTIVE")
                    .ifPresent(p -> {
                        // FIX 2: Đổi Enum thành chuỗi "CANCELLED"
                        p.setStatus("CANCELLED");
                        workoutPlanRepository.save(p);
                    });

            WorkoutPlan officialPlan = WorkoutPlan.builder()
                    .name("Lịch tập AI: " + aiPlanRecord.getGoal())
                    .description(root.path("advice").asText())
                    .member(member)
                    .startDate(LocalDateTime.now())
                    // FIX 3: Đổi Enum thành chuỗi "ACTIVE"
                    .status("ACTIVE")
                    .build();

            Set<WorkoutSession> sessions = new LinkedHashSet<>();
            JsonNode scheduleNode = root.path("workoutSchedule");

            for (JsonNode dayNode : scheduleNode) {
                WorkoutSession session = WorkoutSession.builder()
                        .dayOfWeek(dayNode.path("day").asText())
                        .focusArea(dayNode.path("focus").asText())
                        .workoutPlan(officialPlan)
                        .build();

                Set<WorkoutDetail> details = new LinkedHashSet<>();
                JsonNode exercisesNode = dayNode.path("exercises");

                for (JsonNode exNode : exercisesNode) {
                    WorkoutDetail detail = WorkoutDetail.builder()
                            // FIX 4: Đổi exercise_name thành exerciseName chuẩn camelCase
                            .exerciseName(exNode.path("name").asText())
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
            log.info("===> Lịch tập AI đã trở thành lịch chính thức cho hội viên: {}", member.getFullName());

        } catch (Exception e) {
            log.error("Lỗi Kích hoạt: {}", e.getMessage());
            throw new RuntimeException("Cấu trúc dữ liệu AI không tương thích để tự động kích hoạt.");
        }
    }

    /**
     * Lấy danh sách phác đồ AI đã từng tư vấn cho hội viên
     */
    @Override
    public List<AiWorkoutPlan> getMemberHistory(String username) {
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên"));
        return aiWorkoutPlanRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    // --- CÁC HÀM HỖ TRỢ (HELPERS) ---
    // Các hàm này được để private để giấu kín logic xử lý bên trong Impl

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
        config.put("temperature", 0.3);
        payload.put("generationConfig", config);
        return payload;
    }

    private String buildPrompt(Member member, HealthMetric metric, AiWorkoutRequest req) {
        return String.format(
                "Bạn là Chuyên gia thể hình cấp cao chuẩn NASM. Hãy thiết kế lộ trình tập luyện cá nhân hóa cho hội viên: %s. " +
                        "Thông số hiện tại: Cân nặng %.1fkg, Chiều cao %.1fcm, BMI %.1f. Mục tiêu: %s. " +
                        "Tình trạng chấn thương: %s. Trình độ kinh nghiệm: %s. Thiết bị sẵn có: %s. " +
                        "YÊU CẦU QUAN TRỌNG: Chỉ trả về duy nhất 1 chuỗi JSON thuần túy (không giải thích), cấu trúc chính xác như sau: " +
                        "{\"disclaimer\": \"...\", \"advice\": \"...\", \"nutritionPlan\": {\"targetCalories\": 2000}, " +
                        "\"workoutSchedule\": [{\"day\": \"Thứ...\", \"focus\": \"...\", \"exercises\": [{\"name\": \"...\", \"sets\": 3, \"reps\": \"12\", \"notes\": \"...\"}]}]}",
                member.getFullName(), metric.getWeight(), metric.getHeight(), metric.getBmi(),
                req.getGoal(), (req.getInjuries() != null && !req.getInjuries().isEmpty() ? req.getInjuries() : "Không có"),
                req.getFitnessLevel(), (req.getEquipment() != null && !req.getEquipment().isEmpty() ? req.getEquipment() : "Phòng tập Gym đầy đủ")
        );
    }
}
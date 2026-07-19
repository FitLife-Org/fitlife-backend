package com.fitlife.ai.dto.response;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.AiProvider;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.enums.ExperienceLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSuggestionDetailResponse {

    private Long id;

    private Long memberId;

    private String memberCode;

    private String memberName;

    private Long latestBodyMetricId;

    private AiSuggestionType suggestionType;

    private String goal;

    private ExperienceLevel experienceLevel;

    private ActivityLevel activityLevel;

    private Integer workoutDaysPerWeek;

    private Integer workoutDurationMinutes;

    private String userNote;

    private String preferredLanguage;

    /**
     * Snapshot đầu vào tại thời điểm request.
     *
     * Không chứa email, phone hoặc dữ liệu xác thực.
     */
    private Map<String, Object> inputSnapshot;

    /**
     * Response JSON đã normalize.
     *
     * Chỉ trả trong detail, không trả ở history.
     */
    private Map<String, Object> aiResponse;

    private String summary;

    private String warningMessage;

    private AiProvider provider;

    private String modelName;

    private String promptVersion;

    private AiSuggestionStatus status;

    /**
     * Mã lỗi nội bộ an toàn.
     */
    private String errorCode;

    /**
     * Message đã sanitize.
     */
    private String errorMessage;

    private Long appliedWorkoutPlanId;

    private Long appliedNutritionPlanId;

    private List<AiPlanItemResponse> items;

    private AiFeedbackResponse feedback;

    private LocalDateTime requestedAt;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
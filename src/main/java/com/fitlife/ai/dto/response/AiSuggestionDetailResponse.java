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
     * Snapshot đầu vào tại thời điểm tạo suggestion.
     */
    private Map<String, Object> inputSnapshot;

    /**
     * Snapshot knowledge retrieval / RAG.
     *
     * Dùng để audit:
     * - collection
     * - topK
     * - fallback
     * - fallbackReason
     * - chunks
     */
    private Map<String, Object> contextSnapshot;

    /**
     * AI response đã parse và normalize.
     */
    private Map<String, Object> aiResponse;

    private String summary;

    private String warningMessage;

    private AiProvider provider;

    private String modelName;

    private String promptVersion;

    private AiSuggestionStatus status;

    private String errorCode;

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
package com.fitlife.ai.entity;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.enums.ExperienceLevel;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.member.entity.Member;
import com.fitlife.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "ai_suggestions",
        indexes = {
                @Index(name = "idx_ai_suggestions_member", columnList = "member_id"),
                @Index(name = "idx_ai_suggestions_type", columnList = "suggestion_type"),
                @Index(name = "idx_ai_suggestions_status", columnList = "status"),
                @Index(name = "idx_ai_suggestions_goal", columnList = "goal"),
                @Index(name = "idx_ai_suggestions_created_at", columnList = "created_at"),
                @Index(name = "idx_ai_suggestions_member_status", columnList = "member_id, status")
        }
)
public class AiSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Member yêu cầu AI tạo gợi ý.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * Body metric mới nhất tại thời điểm tạo AI suggestion.
     * Có thể null nếu member chưa nhập body metric.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "latest_body_metric_id")
    private BodyMetric latestBodyMetric;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggestion_type", nullable = false, length = 50)
    private AiSuggestionType suggestionType;

    /**
     * Mục tiêu tập luyện.
     * Tạm để String để linh hoạt với fitness_goal hiện tại trong members.
     * Ví dụ: LOSE_WEIGHT, GAIN_MUSCLE, MAINTAIN, IMPROVE_HEALTH.
     */
    @Column(name = "goal", nullable = false, length = 100)
    private String goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", length = 50)
    private ExperienceLevel experienceLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", length = 50)
    private ActivityLevel activityLevel;

    @Column(name = "workout_days_per_week")
    private Integer workoutDaysPerWeek;

    @Column(name = "workout_duration_minutes")
    private Integer workoutDurationMinutes;

    @Column(name = "user_note", columnDefinition = "TEXT")
    private String userNote;

    /**
     * JSON lưu dữ liệu đầu vào gửi cho AI.
     * MySQL column type: JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_snapshot", nullable = false, columnDefinition = "json")
    private String inputSnapshot;

    /**
     * JSON lưu kết quả AI trả về.
     * MySQL column type: JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_response", columnDefinition = "json")
    private String aiResponse;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "warning_message", columnDefinition = "TEXT")
    private String warningMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AiSuggestionStatus status = AiSuggestionStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Tạm thời để Long, chưa FK cứng để tránh phụ thuộc bảng workout/nutrition hiện tại.
     */
    @Column(name = "applied_workout_plan_id")
    private Long appliedWorkoutPlanId;

    @Column(name = "applied_nutrition_plan_id")
    private Long appliedNutritionPlanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = AiSuggestionStatus.PENDING;
        }

        if (deleted == null) {
            deleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
package com.fitlife.ai.entity;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.AiProvider;
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
                @Index(
                        name = "idx_ai_suggestions_member",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_ai_suggestions_type",
                        columnList = "suggestion_type"
                ),
                @Index(
                        name = "idx_ai_suggestions_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_ai_suggestions_goal",
                        columnList = "goal"
                ),
                @Index(
                        name = "idx_ai_suggestions_created_at",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_ai_suggestions_member_status",
                        columnList = "member_id, status"
                ),
                @Index(
                        name = "idx_ai_suggestions_member_created",
                        columnList = "member_id, created_at"
                ),
                @Index(
                        name = "idx_ai_suggestions_daily_usage",
                        columnList = "member_id, requested_at, status, is_deleted"
                ),
                @Index(
                        name = "idx_ai_suggestions_provider",
                        columnList = "provider"
                ),
                @Index(
                        name = "idx_ai_suggestions_prompt_version",
                        columnList = "prompt_version"
                ),
                @Index(
                        name = "idx_ai_suggestions_completed_at",
                        columnList = "completed_at"
                )
        }
)
public class AiSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Member yêu cầu AI tạo gợi ý.
     *
     * Với API self-service, member này phải được xác định
     * từ SecurityContext, không nhận memberId từ request.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_ai_suggestions_member"
            )
    )
    private Member member;

    /**
     * Body Metric mới nhất tại thời điểm gửi yêu cầu.
     *
     * Có thể null khi Member chưa nhập Body Metric.
     * Dữ liệu thực tế dùng cho AI vẫn được lưu trong inputSnapshot.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "latest_body_metric_id",
            foreignKey = @ForeignKey(
                    name = "fk_ai_suggestions_body_metric"
            )
    )
    private BodyMetric latestBodyMetric;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "suggestion_type",
            nullable = false,
            length = 50
    )
    private AiSuggestionType suggestionType;

    /**
     * Mục tiêu luyện tập tại thời điểm request.
     *
     * Tạm giữ String để tương thích dữ liệu Member hiện tại
     * và tránh refactor FitnessGoal trong cùng commit.
     *
     * DTO request phải giới hạn giá trị hợp lệ.
     */
    @Column(
            name = "goal",
            nullable = false,
            length = 100
    )
    private String goal;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "experience_level",
            length = 50
    )
    private ExperienceLevel experienceLevel;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "activity_level",
            length = 50
    )
    private ActivityLevel activityLevel;

    @Column(name = "workout_days_per_week")
    private Integer workoutDaysPerWeek;

    @Column(name = "workout_duration_minutes")
    private Integer workoutDurationMinutes;

    @Column(
            name = "user_note",
            columnDefinition = "TEXT"
    )
    private String userNote;

    /**
     * Ngôn ngữ Member mong muốn.
     *
     * P0:
     * - vi: tiếng Việt
     * - en: tiếng Anh
     *
     * Output mặc định của FitLife vẫn ưu tiên tiếng Việt.
     */
    @Builder.Default
    @Column(
            name = "preferred_language",
            nullable = false,
            length = 10
    )
    private String preferredLanguage = "vi";

    /**
     * Snapshot toàn bộ dữ liệu đầu vào trước khi gọi AI.
     *
     * Bao gồm:
     * - User profile cần thiết
     * - Member profile
     * - Body Metric mới nhất
     * - FullPlanRequest
     *
     * Không build JSON thủ công. Phải dùng ObjectMapper.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "input_snapshot",
            nullable = false,
            columnDefinition = "json"
    )
    private String inputSnapshot;

    /**
     * Metadata context được retrieve từ Qdrant.
     *
     * Không bắt buộc lưu toàn bộ nội dung tài liệu.
     * Có thể lưu:
     * - collection
     * - topK
     * - chunkId
     * - documentId
     * - title
     * - score
     * - fallback
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "context_snapshot",
            columnDefinition = "json"
    )
    private String contextSnapshot;

    /**
     * Raw JSON response sau khi đã làm sạch.
     *
     * Không nên lưu markdown fence hoặc text thừa
     * trước và sau JSON.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "ai_response",
            columnDefinition = "json"
    )
    private String aiResponse;

    @Column(
            name = "summary",
            columnDefinition = "TEXT"
    )
    private String summary;

    @Column(
            name = "warning_message",
            columnDefinition = "TEXT"
    )
    private String warningMessage;

    /**
     * Nhà cung cấp AI.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "provider",
            nullable = false,
            length = 30
    )
    private AiProvider provider = AiProvider.GEMINI;

    /**
     * Tên model thực tế tại thời điểm xử lý.
     *
     * Ví dụ lấy từ GeminiProperties.model.
     */
    @Column(
            name = "model_name",
            length = 100
    )
    private String modelName;

    /**
     * Phiên bản prompt.
     *
     * Ví dụ:
     * FULL_PLAN_V1
     * BODY_ANALYSIS_V1
     */
    @Column(
            name = "prompt_version",
            length = 50
    )
    private String promptVersion;

    /**
     * ID request do provider trả về, nếu có.
     */
    @Column(
            name = "provider_request_id",
            length = 255
    )
    private String providerRequestId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private AiSuggestionStatus status =
            AiSuggestionStatus.PENDING;

    /**
     * Mã lỗi nội bộ, dùng cho debug và thống kê.
     *
     * Ví dụ:
     * AI_PROVIDER_TIMEOUT
     * AI_RESPONSE_INVALID
     * AI_PARSE_FAILED
     */
    @Column(
            name = "error_code",
            length = 100
    )
    private String errorCode;

    /**
     * Thông tin lỗi đã được làm sạch.
     *
     * Không lưu:
     * - API key
     * - toàn bộ stack trace
     * - dữ liệu nhạy cảm
     */
    @Column(
            name = "error_message",
            columnDefinition = "TEXT"
    )
    private String errorMessage;

    /**
     * Thời điểm Backend tiếp nhận yêu cầu AI.
     */
    @Column(
            name = "requested_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime requestedAt;

    /**
     * Thời điểm request chuyển sang SUCCESS hoặc FAILED.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * ID Workout Plan đã được tạo từ Suggestion.
     *
     * Chưa tạo FK cứng trong giai đoạn này vì schema
     * Workout Plan vẫn đang được hoàn thiện.
     */
    @Column(name = "applied_workout_plan_id")
    private Long appliedWorkoutPlanId;

    /**
     * ID Nutrition Plan đã được tạo từ Suggestion.
     */
    @Column(name = "applied_nutrition_plan_id")
    private Long appliedNutritionPlanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            foreignKey = @ForeignKey(
                    name = "fk_ai_suggestions_created_by"
            )
    )
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "updated_by",
            foreignKey = @ForeignKey(
                    name = "fk_ai_suggestions_updated_by"
            )
    )
    private User updatedBy;

    @Builder.Default
    @Column(
            name = "is_deleted",
            nullable = false
    )
    private Boolean deleted = false;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
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

        if (requestedAt == null) {
            requestedAt = now;
        }

        if (status == null) {
            status = AiSuggestionStatus.PENDING;
        }

        if (provider == null) {
            provider = AiProvider.GEMINI;
        }

        if (preferredLanguage == null
                || preferredLanguage.isBlank()) {
            preferredLanguage = "vi";
        }

        if (deleted == null) {
            deleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Chuyển suggestion sang SUCCESS.
     */
    public void markSuccess() {
        this.status = AiSuggestionStatus.SUCCESS;
        this.completedAt = LocalDateTime.now();
        this.errorCode = null;
        this.errorMessage = null;
    }

    /**
     * Chuyển suggestion sang FAILED.
     */
    public void markFailed(
            String errorCode,
            String errorMessage
    ) {
        this.status = AiSuggestionStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
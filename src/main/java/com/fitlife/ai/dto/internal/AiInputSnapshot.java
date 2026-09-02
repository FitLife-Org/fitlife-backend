package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Toàn bộ dữ liệu đầu vào được đóng băng tại thời điểm
 * tạo một AI Suggestion.
 *
 * Snapshot này được:
 * - dùng để build prompt;
 * - lưu cùng AiSuggestion;
 * - phục vụ audit;
 * - tái hiện dữ liệu đầu vào khi cần kiểm tra lỗi.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInputSnapshot {

    /**
     * Dữ liệu User tối thiểu cần thiết cho AI.
     */
    private AiInputUserSnapshot user;

    /**
     * Dữ liệu nghiệp vụ Member.
     */
    private AiInputMemberSnapshot member;

    /**
     * Body Metric mới nhất theo recordedAt.
     *
     * Sau Batch 3, các nghiệp vụ sinh kế hoạch AI
     * yêu cầu object này không được null.
     */
    private AiInputBodyMetricSnapshot latestBodyMetric;

    /**
     * Dữ liệu do Member gửi trong request hiện tại.
     */
    private AiInputRequestSnapshot request;

    /**
     * Thời điểm snapshot được tạo.
     */
    private LocalDateTime capturedAt;
}
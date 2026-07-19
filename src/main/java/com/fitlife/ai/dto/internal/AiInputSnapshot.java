package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Toàn bộ dữ liệu đầu vào được đóng băng tại thời điểm
 * tạo một AI Suggestion.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInputSnapshot {

    private AiInputUserSnapshot user;

    private AiInputMemberSnapshot member;

    /**
     * Có thể null nếu Member chưa nhập Body Metric.
     */
    private AiInputBodyMetricSnapshot latestBodyMetric;

    private AiInputRequestSnapshot request;
}
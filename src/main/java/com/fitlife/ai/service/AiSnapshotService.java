package com.fitlife.ai.service;

import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.member.entity.Member;

/**
 * Xây dựng snapshot đầu vào cho các yêu cầu AI.
 *
 * Snapshot lưu lại đúng dữ liệu Member, Body Metric và request
 * tại thời điểm tạo AI Suggestion.
 */
public interface AiSnapshotService {

    /**
     * Xây dựng snapshot cho Full Plan.
     *
     * @param member           Member hiện tại
     * @param latestBodyMetric Body Metric mới nhất, có thể null
     * @param request          yêu cầu tạo Full Plan
     * @return snapshot hoàn chỉnh
     */
    AiInputSnapshot buildFullPlanSnapshot(
            Member member,
            BodyMetric latestBodyMetric,
            AiFullPlanRequest request
    );

    /**
     * Xây dựng snapshot cho Body Analysis.
     *
     * Body Metric là bắt buộc đối với luồng này.
     *
     * @param member           Member hiện tại
     * @param latestBodyMetric Body Metric mới nhất
     * @param request          yêu cầu phân tích cơ thể
     * @return snapshot hoàn chỉnh
     */
    AiInputSnapshot buildBodyAnalysisSnapshot(
            Member member,
            BodyMetric latestBodyMetric,
            AiBodyAnalysisRequest request
    );
}
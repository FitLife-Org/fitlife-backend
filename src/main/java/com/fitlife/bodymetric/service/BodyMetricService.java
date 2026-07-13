package com.fitlife.bodymetric.service;

import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.dto.request.BodyMetricUpdateRequest;
import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BodyMetricService {

    // =========================
    // Admin / Staff
    // =========================

    BodyMetricResponse createByAdmin(BodyMetricCreateRequest request);

    PageResponse<BodyMetricResponse> getBodyMetricsForAdmin(
            Long memberId,
            String keyword,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    BodyMetricResponse getBodyMetricDetailForAdmin(Long id);

    PageResponse<BodyMetricResponse> getBodyMetricsByMemberForAdmin(
            Long memberId,
            Pageable pageable
    );

    BodyMetricResponse getLatestBodyMetricByMemberForAdmin(Long memberId);

    BodyMetricResponse updateByAdmin(Long id, BodyMetricUpdateRequest request);

    void deleteByAdmin(Long id);

    // =========================
    // Member - My Body Metric
    // =========================

    PageResponse<BodyMetricResponse> getMyBodyMetrics(Pageable pageable);

    BodyMetricResponse getMyBodyMetricDetail(Long id);

    BodyMetricResponse getLatestMyBodyMetric();

    List<BodyMetricResponse> getMyBodyMetricHistory(
            LocalDateTime from,
            LocalDateTime to
    );
}
package com.fitlife.bodymetric.service;

import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.dto.request.BodyMetricUpdateRequest;
import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BodyMetricService {

    BodyMetricResponse createMyBodyMetric(BodyMetricCreateRequest request);

    BodyMetricResponse createForMember(Long memberId, BodyMetricCreateRequest request);

    PageResponse<BodyMetricResponse> getMyBodyMetrics(Pageable pageable);

    PageResponse<BodyMetricResponse> getBodyMetricsByMember(Long memberId, Pageable pageable);

    BodyMetricResponse getMyBodyMetricDetail(Long id);

    BodyMetricResponse getLatestMyBodyMetric();

    BodyMetricResponse getLatestBodyMetricByMember(Long memberId);

    List<BodyMetricResponse> getMyBodyMetricHistory(LocalDateTime from, LocalDateTime to);

    BodyMetricResponse updateMyBodyMetric(Long id, BodyMetricUpdateRequest request);

    void deleteMyBodyMetric(Long id);


    BodyMetricResponse createByAdmin(BodyMetricCreateRequest request);
}
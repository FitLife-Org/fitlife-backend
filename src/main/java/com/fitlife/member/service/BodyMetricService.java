package com.fitlife.member.service;

import com.fitlife.member.dto.BodyMetricRequest;
import com.fitlife.member.entity.BodyMetric;

import java.util.List;

public interface BodyMetricService {
    BodyMetric addBodyMetric(String username, BodyMetricRequest request);

    List<BodyMetric> getMemberHistory(String username);
}

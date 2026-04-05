package com.fitlife.member;

import com.fitlife.member.dto.HealthMetricRequest;
import com.fitlife.member.entity.HealthMetric;

import java.util.List;

public interface HealthMetricService {
    HealthMetric addHealthMetric(String username, HealthMetricRequest request);

    List<HealthMetric> getMemberHistory(String username);
}

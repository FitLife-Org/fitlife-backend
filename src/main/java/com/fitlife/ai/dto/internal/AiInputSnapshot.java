package com.fitlife.ai.dto.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AiInputSnapshot {

    private AiInputMemberSnapshot member;

    private AiInputUserSnapshot user;

    private AiInputBodyMetricSnapshot latestBodyMetric;

    private AiInputRequestSnapshot request;
}
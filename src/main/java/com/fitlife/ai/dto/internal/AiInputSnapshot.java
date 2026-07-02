package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiInputSnapshot {

    private AiInputMemberSnapshot member;

    private AiInputUserSnapshot user;

    private AiInputBodyMetricSnapshot latestBodyMetric;

    private AiInputRequestSnapshot request;
}
package com.fitlife.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AiUsageTodayResponse {

    private final int dailyLimit;
    private final long used;
    private final long remaining;
    private final LocalDateTime resetAt;
}
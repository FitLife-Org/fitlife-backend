package com.fitlife.ai.dto.internal;

import com.fitlife.ai.enums.AiProvider;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Kết quả chuẩn hóa trả về từ AI provider.
 *
 * Không đưa raw provider response ra API public.
 */
@Getter
@Builder
public class AiProviderResult {

    private final AiProvider provider;
    private final String modelName;
    private final String providerRequestId;
    private final String rawResponse;
    private final LocalDateTime requestedAt;
    private final LocalDateTime completedAt;
}
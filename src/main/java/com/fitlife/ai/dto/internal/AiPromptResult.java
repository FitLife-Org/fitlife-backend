package com.fitlife.ai.dto.internal;

import com.fitlife.ai.enums.AiPromptVersion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiPromptResult {

    private final AiPromptVersion version;

    private final String prompt;

    /**
     * Knowledge context đã được sử dụng để build prompt.
     * Có thể rỗng hoặc fallback khi Qdrant không khả dụng.
     */
    private final AiContextSnapshot contextSnapshot;

    public String getVersionCode() {
        return version == null
                ? null
                : version.getCode();
    }
}
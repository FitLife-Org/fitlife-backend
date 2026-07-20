package com.fitlife.ai.dto.internal;

import com.fitlife.ai.enums.AiPromptVersion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiPromptResult {
    private final AiPromptVersion version;
    private final String prompt;

    public String getVersionCode() {
        return version == null ? null : version.getCode();
    }
}
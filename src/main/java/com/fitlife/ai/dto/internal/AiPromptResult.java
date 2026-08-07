package com.fitlife.ai.dto.internal;

import com.fitlife.ai.enums.AiPromptVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kết quả sau khi Prompt Builder hoàn tất.
 *
 * Chứa:
 * - phiên bản prompt;
 * - prompt thực tế gửi provider;
 * - context retrieval phục vụ audit.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPromptResult {

    private AiPromptVersion version;

    private String prompt;

    private AiContextSnapshot contextSnapshot;

    public String getVersionCode() {
        return version == null
                ? null
                : version.name();
    }

    public boolean hasRetrievalContext() {
        return contextSnapshot != null &&
                contextSnapshot.hasKnowledge();
    }

    public boolean isFallbackContext() {
        return contextSnapshot != null &&
                Boolean.TRUE.equals(
                        contextSnapshot.getFallback()
                );
    }
}
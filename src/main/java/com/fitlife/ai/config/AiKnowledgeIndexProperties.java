package com.fitlife.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(
        prefix = "fitlife.ai.knowledge-index"
)
public class AiKnowledgeIndexProperties {

    /**
     * Bật chức năng index knowledge.
     */
    private boolean enabled = true;

    /**
     * Index lại tất cả knowledge active khi startup.
     */
    private boolean reindexOnStartup = false;

    /**
     * Không dừng ứng dụng khi một knowledge index lỗi.
     */
    private boolean continueOnError = true;
}
package com.fitlife.ai.knowledge.runner;

import com.fitlife.ai.config.AiKnowledgeIndexProperties;
import com.fitlife.ai.knowledge.service.AiKnowledgeIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiKnowledgeIndexRunner
        implements ApplicationRunner {

    private final AiKnowledgeIndexProperties
            properties;

    private final AiKnowledgeIndexService
            indexService;

    @Override
    public void run(
            ApplicationArguments args
    ) {
        if (!properties.isEnabled()) {
            log.info(
                    "AI knowledge indexing is disabled"
            );

            return;
        }

        if (!properties
                .isReindexOnStartup()) {
            log.info(
                    "AI knowledge reindex on startup is disabled"
            );

            return;
        }

        try {
            int successCount =
                    indexService.reindexAll();

            log.info(
                    "AI knowledge startup reindex completed. "
                            + "successCount={}",
                    successCount
            );

        } catch (Exception exception) {
            log.error(
                    "AI knowledge startup reindex failed. "
                            + "reason={}",
                    exception.getMessage(),
                    exception
            );

            if (!properties
                    .isContinueOnError()) {
                throw exception;
            }
        }
    }
}
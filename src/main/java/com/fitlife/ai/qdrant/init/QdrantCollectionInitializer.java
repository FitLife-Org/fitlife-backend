package com.fitlife.ai.qdrant.init;

import com.fitlife.ai.config.AiQdrantProperties;
import com.fitlife.ai.qdrant.service.AiQdrantCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QdrantCollectionInitializer
        implements ApplicationRunner {

    private final AiQdrantProperties properties;
    private final AiQdrantCollectionService
            collectionService;

    @Override
    public void run(
            ApplicationArguments args
    ) {
        if (!properties.isEnabled()) {
            log.info(
                    "Qdrant integration is disabled"
            );
            return;
        }

        if (!properties.isInitializeCollection()) {
            log.info(
                    "Qdrant collection initialization is disabled"
            );
            return;
        }

        try {
            collectionService.ensureCollection();
        } catch (Exception exception) {
            if (properties.isFailFast()) {
                throw exception;
            }

            log.error(
                    "Qdrant initialization failed; "
                            + "application will continue without RAG: {}",
                    exception.getMessage(),
                    exception
            );
        }
    }
}

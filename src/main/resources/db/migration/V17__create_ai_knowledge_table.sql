CREATE TABLE ai_knowledge (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              code VARCHAR(100) NOT NULL UNIQUE,
                              title VARCHAR(200) NOT NULL,
                              content LONGTEXT NOT NULL,
                              category VARCHAR(50) NOT NULL,
                              goal VARCHAR(50),
                              experience_level VARCHAR(50),
                              language VARCHAR(10) NOT NULL DEFAULT 'vi',
                              active BOOLEAN NOT NULL DEFAULT TRUE,
                              index_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                              qdrant_point_id VARCHAR(100),
                              indexed_at DATETIME,
                              index_error VARCHAR(500),
                              is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                              created_at DATETIME NOT NULL,
                              updated_at DATETIME NOT NULL
);

CREATE INDEX idx_ai_knowledge_category
    ON ai_knowledge(category);

CREATE INDEX idx_ai_knowledge_active
    ON ai_knowledge(active, is_deleted);

CREATE INDEX idx_ai_knowledge_index_status
    ON ai_knowledge(index_status);
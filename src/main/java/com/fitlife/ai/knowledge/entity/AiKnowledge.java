package com.fitlife.ai.knowledge.entity;

import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ai_knowledge",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ai_knowledge_code",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiKnowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = 100
    )
    private String code;

    @Column(
            name = "title",
            nullable = false,
            length = 200
    )
    private String title;

    @Lob
    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "category",
            nullable = false,
            length = 50
    )
    private AiKnowledgeCategory category;

    @Column(name = "goal", length = 50)
    private String goal;

    @Column(
            name = "experience_level",
            length = 50
    )
    private String experienceLevel;

    @Builder.Default
    @Column(
            name = "language",
            nullable = false,
            length = 10
    )
    private String language = "vi";

    @Builder.Default
    @Column(
            name = "active",
            nullable = false
    )
    private Boolean active = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "index_status",
            nullable = false,
            length = 30
    )
    private AiKnowledgeIndexStatus indexStatus =
            AiKnowledgeIndexStatus.PENDING;

    @Column(
            name = "qdrant_point_id",
            length = 100
    )
    private String qdrantPointId;

    @Column(name = "indexed_at")
    private LocalDateTime indexedAt;

    @Column(
            name = "index_error",
            length = 500
    )
    private String indexError;

    @Builder.Default
    @Column(
            name = "is_deleted",
            nullable = false
    )
    private Boolean deleted = false;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (active == null) {
            active = true;
        }

        if (deleted == null) {
            deleted = false;
        }

        if (indexStatus == null) {
            indexStatus = AiKnowledgeIndexStatus.PENDING;
        }

        if (language == null || language.isBlank()) {
            language = "vi";
        }
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
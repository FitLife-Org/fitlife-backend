package com.fitlife.ai.knowledge.repository;

import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AiKnowledgeRepository
        extends JpaRepository<AiKnowledge, Long> {

    boolean existsByCodeIgnoreCaseAndDeletedFalse(
            String code
    );

    Optional<AiKnowledge>
    findByIdAndDeletedFalse(
            Long id
    );

    List<AiKnowledge>
    findAllByDeletedFalseAndActiveTrueOrderByIdAsc();

    default List<AiKnowledge>
    findAllByDeletedFalseAndActiveTrue() {
        return findAllByDeletedFalseAndActiveTrueOrderByIdAsc();
    }

    @Query("""
            SELECT k
            FROM AiKnowledge k
            WHERE k.deleted = false
              AND (
                    :keyword IS NULL
                    OR LOWER(k.code)
                        LIKE LOWER(
                            CONCAT('%', :keyword, '%')
                        )
                    OR LOWER(k.title)
                        LIKE LOWER(
                            CONCAT('%', :keyword, '%')
                        )
              )
              AND (
                    :category IS NULL
                    OR k.category = :category
              )
              AND (
                    :indexStatus IS NULL
                    OR k.indexStatus = :indexStatus
              )
              AND (
                    :active IS NULL
                    OR k.active = :active
              )
            """)
    Page<AiKnowledge> search(
            @Param("keyword")
            String keyword,

            @Param("category")
            AiKnowledgeCategory category,

            @Param("indexStatus")
            AiKnowledgeIndexStatus indexStatus,

            @Param("active")
            Boolean active,

            Pageable pageable
    );
}
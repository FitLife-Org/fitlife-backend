package com.fitlife.ai.knowledge.repository;

import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AiKnowledgeRepository
        extends
        JpaRepository<AiKnowledge, Long>,
        JpaSpecificationExecutor<AiKnowledge> {

    Optional<AiKnowledge> findByIdAndDeletedFalse(
            Long id
    );

    Optional<AiKnowledge> findByCodeIgnoreCaseAndDeletedFalse(
            String code
    );

    boolean existsByCodeIgnoreCaseAndDeletedFalse(
            String code
    );

    boolean existsByCodeIgnoreCaseAndIdNotAndDeletedFalse(
            String code,
            Long id
    );

    List<AiKnowledge>
    findByActiveTrueAndDeletedFalseOrderByUpdatedAtDesc();

    List<AiKnowledge>
    findByActiveTrueAndDeletedFalseAndIndexStatusNotOrderByUpdatedAtAsc(
            AiKnowledgeIndexStatus indexStatus
    );

    List<AiKnowledge>
    findByDeletedFalseAndIndexStatusOrderByUpdatedAtAsc(
            AiKnowledgeIndexStatus indexStatus
    );

    long countByDeletedFalse();

    long countByActiveTrueAndDeletedFalse();

    long countByDeletedFalseAndIndexStatus(
            AiKnowledgeIndexStatus indexStatus
    );
}
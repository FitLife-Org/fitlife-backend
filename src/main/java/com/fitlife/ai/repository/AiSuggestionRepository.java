package com.fitlife.ai.repository;

import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AiSuggestionRepository extends JpaRepository<AiSuggestion, Long> {

    Page<AiSuggestion> findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(
            Long memberId,
            Pageable pageable
    );

    Optional<AiSuggestion> findByIdAndMemberIdAndDeletedFalse(
            Long id,
            Long memberId
    );

    Page<AiSuggestion> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    long countByMemberIdAndCreatedAtBetweenAndDeletedFalse(
            Long memberId,
            LocalDateTime from,
            LocalDateTime to
    );

    Page<AiSuggestion> findBySuggestionTypeAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    );
}
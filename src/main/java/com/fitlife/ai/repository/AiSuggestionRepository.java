package com.fitlife.ai.repository;

import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface AiSuggestionRepository
        extends JpaRepository<AiSuggestion, Long> {

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user"
            }
    )
    Optional<AiSuggestion>
    findResponseById(
            Long id
    );

    Page<AiSuggestion>
    findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(
            Long memberId,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "latestBodyMetric"
            }
    )
    Optional<AiSuggestion>
    findByIdAndMemberIdAndDeletedFalse(
            Long id,
            Long memberId
    );

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "latestBodyMetric"
            }
    )
    @Query("""
            SELECT suggestion
            FROM AiSuggestion suggestion
            WHERE suggestion.id = :id
              AND suggestion.member.id = :memberId
              AND suggestion.deleted = false
            """)
    Optional<AiSuggestion> findDetailByIdAndMemberId(
            @Param("id")
            Long id,

            @Param("memberId")
            Long memberId
    );

    Page<AiSuggestion>
    findByMemberIdAndSuggestionTypeAndDeletedFalseOrderByCreatedAtDesc(
            Long memberId,
            AiSuggestionType suggestionType,
            Pageable pageable
    );

    Page<AiSuggestion>
    findByMemberIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            Long memberId,
            AiSuggestionStatus status,
            Pageable pageable
    );

    Page<AiSuggestion>
    findByMemberIdAndSuggestionTypeAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            Long memberId,
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(s)
            FROM AiSuggestion s
            WHERE s.member.id = :memberId
              AND s.requestedAt >= :from
              AND s.requestedAt < :to
            """)
    long countTodayUsage(
            @Param("memberId")
            Long memberId,

            @Param("from")
            LocalDateTime from,

            @Param("to")
            LocalDateTime to
    );

    long countByMemberIdAndStatusInAndRequestedAtBetweenAndDeletedFalse(
            Long memberId,
            Collection<AiSuggestionStatus> statuses,
            LocalDateTime from,
            LocalDateTime to
    );

    Page<AiSuggestion>
    findByDeletedFalseOrderByCreatedAtDesc(
            Pageable pageable
    );

    Page<AiSuggestion>
    findBySuggestionTypeAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    );

    Page<AiSuggestion>
    findBySuggestionTypeAndDeletedFalseOrderByCreatedAtDesc(
            AiSuggestionType suggestionType,
            Pageable pageable
    );

    Page<AiSuggestion>
    findByStatusAndDeletedFalseOrderByCreatedAtDesc(
            AiSuggestionStatus status,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT suggestion
            FROM AiSuggestion suggestion
            WHERE suggestion.id = :id
            """)
    Optional<AiSuggestion> findByIdForUpdate(
            @Param("id")
            Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT suggestion
            FROM AiSuggestion suggestion
            WHERE suggestion.id = :id
              AND suggestion.member.id = :memberId
              AND suggestion.deleted = false
            """)
    Optional<AiSuggestion> findOwnedByIdForUpdate(
            @Param("id")
            Long id,

            @Param("memberId")
            Long memberId
    );
}
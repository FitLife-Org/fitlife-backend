package com.fitlife.ai.repository;

import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface AiSuggestionRepository
        extends JpaRepository<AiSuggestion, Long> {

    // =====================================================
    // Member history
    // =====================================================

    /**
     * Lấy lịch sử AI Suggestion của Member.
     */
    Page<AiSuggestion>
    findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(
            Long memberId,
            Pageable pageable
    );

    /**
     * Lấy chi tiết Suggestion theo ID và Member sở hữu.
     *
     * Query này đồng thời kiểm tra ownership.
     */
    Optional<AiSuggestion>
    findByIdAndMemberIdAndDeletedFalse(
            Long id,
            Long memberId
    );

    // =====================================================
    // Member filters
    // =====================================================

    /**
     * Lọc lịch sử theo Suggestion Type.
     */
    Page<AiSuggestion>
    findByMemberIdAndSuggestionTypeAndDeletedFalseOrderByCreatedAtDesc(
            Long memberId,
            AiSuggestionType suggestionType,
            Pageable pageable
    );

    /**
     * Lọc lịch sử theo trạng thái.
     */
    Page<AiSuggestion>
    findByMemberIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            Long memberId,
            AiSuggestionStatus status,
            Pageable pageable
    );

    /**
     * Lọc lịch sử theo loại và trạng thái.
     */
    Page<AiSuggestion>
    findByMemberIdAndSuggestionTypeAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            Long memberId,
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    );

    // =====================================================
    // Daily usage
    // =====================================================

    /**
     * Đếm tổng số request AI trong một khoảng thời gian.
     *
     * Dùng requestedAt thay vì createdAt vì đây là thời điểm
     * hệ thống chính thức tiếp nhận yêu cầu AI.
     */
    long countByMemberIdAndRequestedAtBetweenAndDeletedFalse(
            Long memberId,
            LocalDateTime from,
            LocalDateTime to
    );

    /**
     * Đếm request AI theo nhóm trạng thái.
     *
     * Có thể dùng để chỉ tính:
     * - PENDING
     * - SUCCESS
     * - APPLIED
     *
     * và bỏ qua FAILED nếu business rule quyết định
     * lỗi hệ thống không tiêu tốn lượt.
     */
    long countByMemberIdAndStatusInAndRequestedAtBetweenAndDeletedFalse(
            Long memberId,
            Collection<AiSuggestionStatus> statuses,
            LocalDateTime from,
            LocalDateTime to
    );

    // =====================================================
    // Admin
    // =====================================================

    /**
     * Admin xem toàn bộ AI Suggestion.
     */
    Page<AiSuggestion>
    findByDeletedFalseOrderByCreatedAtDesc(
            Pageable pageable
    );

    /**
     * Admin lọc theo type và status.
     */
    Page<AiSuggestion>
    findBySuggestionTypeAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    );

    /**
     * Admin lọc theo loại Suggestion.
     */
    Page<AiSuggestion>
    findBySuggestionTypeAndDeletedFalseOrderByCreatedAtDesc(
            AiSuggestionType suggestionType,
            Pageable pageable
    );

    /**
     * Admin lọc theo trạng thái.
     */
    Page<AiSuggestion>
    findByStatusAndDeletedFalseOrderByCreatedAtDesc(
            AiSuggestionStatus status,
            Pageable pageable
    );
}
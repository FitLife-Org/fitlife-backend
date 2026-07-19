package com.fitlife.ai.repository;

import com.fitlife.ai.entity.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiFeedbackRepository
        extends JpaRepository<AiFeedback, Long> {

    /**
     * Lấy feedback của một Member đối với một AI Suggestion.
     *
     * Dùng cho:
     * - Hiển thị feedback trong màn hình chi tiết.
     * - Kiểm tra quyền sở hữu khi cập nhật feedback.
     */
    Optional<AiFeedback> findByAiSuggestionIdAndMemberId(
            Long aiSuggestionId,
            Long memberId
    );

    /**
     * Kiểm tra Member đã gửi feedback cho Suggestion hay chưa.
     *
     * Mỗi Member chỉ được có một feedback trên một Suggestion.
     */
    boolean existsByAiSuggestionIdAndMemberId(
            Long aiSuggestionId,
            Long memberId
    );

    /**
     * Lấy feedback theo ID và Member sở hữu.
     *
     * Dùng cho API cập nhật feedback.
     */
    Optional<AiFeedback> findByIdAndMemberId(
            Long id,
            Long memberId
    );
}
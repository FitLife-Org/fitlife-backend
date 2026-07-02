package com.fitlife.ai.repository;

import com.fitlife.ai.entity.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {

    Optional<AiFeedback> findByAiSuggestionIdAndMemberId(Long aiSuggestionId, Long memberId);

    boolean existsByAiSuggestionIdAndMemberId(Long aiSuggestionId, Long memberId);
}
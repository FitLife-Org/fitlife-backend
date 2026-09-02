package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.mapper.AiSuggestionMapper;
import com.fitlife.ai.repository.AiPlanItemRepository;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.ai.service.AiSuggestionResponseService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiSuggestionResponseServiceImpl
        implements AiSuggestionResponseService {

    private final AiSuggestionRepository
            aiSuggestionRepository;

    private final AiPlanItemRepository
            aiPlanItemRepository;

    private final AiSuggestionMapper
            aiSuggestionMapper;

    // =====================================================
    // SUMMARY
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public AiSuggestionResponse getSummaryResponse(
            Long suggestionId
    ) {
        AiSuggestion suggestion =
                getSuggestion(
                        suggestionId
                );

        /*
         * Mapper chạy khi transaction vẫn mở.
         *
         * findResponseById() phải fetch:
         * - member
         * - member.user
         *
         * Nhờ vậy mapper có thể đọc:
         * memberCode
         * memberName
         *
         * mà không LazyInitializationException.
         */
        return aiSuggestionMapper
                .toResponse(
                        suggestion
                );
    }

    // =====================================================
    // DETAIL
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public AiSuggestionDetailResponse getDetailResponse(
            Long suggestionId
    ) {
        AiSuggestion suggestion =
                getSuggestion(
                        suggestionId
                );

        List<AiPlanItem> items =
                aiPlanItemRepository
                        .findByAiSuggestionIdOrderBySortOrderAscIdAsc(
                                suggestionId
                        );

        /*
         * Feedback để null ở đây.
         *
         * Endpoint detail chính của member vẫn có thể
         * load feedback bằng service riêng.
         *
         * Với response ngay sau khi generate thì chưa
         * có feedback nên null là đúng nghiệp vụ.
         */
        return aiSuggestionMapper
                .toDetailResponse(
                        suggestion,
                        items,
                        null
                );
    }

    // =====================================================
    // PRIVATE
    // =====================================================

    private AiSuggestion getSuggestion(
            Long suggestionId
    ) {
        validateSuggestionId(
                suggestionId
        );

        return aiSuggestionRepository
                .findResponseById(
                        suggestionId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode
                                        .AI_SUGGESTION_NOT_FOUND
                        )
                );
    }

    private void validateSuggestionId(
            Long suggestionId
    ) {
        if (
                suggestionId == null ||
                        suggestionId <= 0
        ) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_NOT_FOUND
            );
        }
    }
}
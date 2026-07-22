package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.mapper.AiSuggestionMapper;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.ai.service.AiSuggestionResponseService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiSuggestionResponseServiceImpl
        implements AiSuggestionResponseService {

    private final AiSuggestionRepository
            aiSuggestionRepository;

    private final AiSuggestionMapper
            aiSuggestionMapper;

    @Override
    @Transactional(readOnly = true)
    public AiSuggestionResponse getSummaryResponse(
            Long suggestionId
    ) {
        if (suggestionId == null
                || suggestionId <= 0) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_NOT_FOUND
            );
        }

        AiSuggestion suggestion =
                aiSuggestionRepository
                        .findResponseById(
                                suggestionId
                        )
                        .orElseThrow(
                                () -> new AppException(
                                        ErrorCode
                                                .AI_SUGGESTION_NOT_FOUND
                                )
                        );

        /*
         * Mapper chạy khi transaction vẫn còn mở.
         * member và member.user đã được fetch.
         */
        return aiSuggestionMapper.toResponse(
                suggestion
        );
    }
}
package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedNutritionPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutPlanResponse;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.ai.service.AiPlanParserService;
import com.fitlife.ai.service.AiSuggestionPersistenceService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiSuggestionPersistenceServiceImpl
        implements AiSuggestionPersistenceService {

    private static final String DEFAULT_FAILURE_CODE =
            "AI_REQUEST_FAILED";

    private static final String DEFAULT_FAILURE_MESSAGE =
            "Không thể xử lý yêu cầu AI vào lúc này.";

    private final AiSuggestionRepository aiSuggestionRepository;
    private final AiPlanParserService aiPlanParserService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiSuggestion createPending(
            AiSuggestion suggestion
    ) {
        validatePendingSuggestion(
                suggestion
        );

        return aiSuggestionRepository
                .saveAndFlush(
                        suggestion
                );
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiSuggestion markFullPlanSuccess(
            Long suggestionId,
            AiProviderResult providerResult,
            AiGeneratedPlanResponse generatedPlan,
            String warningMessage
    ) {
        AiSuggestion suggestion =
                getPendingSuggestion(
                        suggestionId,
                        AiSuggestionType.FULL_PLAN
                );

        validateProviderResult(
                providerResult
        );

        validateGeneratedResponse(
                generatedPlan
        );

        applySuccessData(
                suggestion,
                providerResult,
                generatedPlan,
                generatedPlan.getSummary(),
                warningMessage
        );

        AiSuggestion saved =
                aiSuggestionRepository
                        .saveAndFlush(
                                suggestion
                        );

        /*
         * Cùng transaction REQUIRES_NEW.
         * Nếu savePlanItems lỗi thì SUCCESS cũng rollback.
         */
        aiPlanParserService.savePlanItems(
                saved,
                generatedPlan
        );

        return saved;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiSuggestion markWorkoutPlanSuccess(
            Long suggestionId,
            AiProviderResult providerResult,
            AiGeneratedWorkoutPlanResponse generated,
            String warningMessage
    ) {
        AiSuggestion suggestion =
                getPendingSuggestion(
                        suggestionId,
                        AiSuggestionType.WORKOUT_PLAN
                );

        validateProviderResult(
                providerResult
        );

        validateGeneratedResponse(
                generated
        );

        applySuccessData(
                suggestion,
                providerResult,
                generated,
                generated.getSummary(),
                warningMessage
        );

        AiSuggestion saved =
                aiSuggestionRepository
                        .saveAndFlush(
                                suggestion
                        );

        aiPlanParserService
                .saveWorkoutPlanItems(
                        saved,
                        generated
                );

        return saved;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiSuggestion markNutritionPlanSuccess(
            Long suggestionId,
            AiProviderResult providerResult,
            AiGeneratedNutritionPlanResponse generated,
            String warningMessage
    ) {
        AiSuggestion suggestion =
                getPendingSuggestion(
                        suggestionId,
                        AiSuggestionType.NUTRITION_PLAN
                );

        validateProviderResult(
                providerResult
        );

        validateGeneratedResponse(
                generated
        );

        applySuccessData(
                suggestion,
                providerResult,
                generated,
                generated.getSummary(),
                warningMessage
        );

        AiSuggestion saved =
                aiSuggestionRepository
                        .saveAndFlush(
                                suggestion
                        );

        aiPlanParserService
                .saveNutritionPlanItems(
                        saved,
                        generated
                );

        return saved;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiSuggestion markBodyAnalysisSuccess(
            Long suggestionId,
            AiProviderResult providerResult,
            AiGeneratedBodyAnalysisResponse analysis,
            String warningMessage
    ) {
        AiSuggestion suggestion =
                getPendingSuggestion(
                        suggestionId,
                        AiSuggestionType.BODY_ANALYSIS
                );

        validateProviderResult(
                providerResult
        );

        validateGeneratedResponse(
                analysis
        );

        applySuccessData(
                suggestion,
                providerResult,
                analysis,
                analysis.getSummary(),
                warningMessage
        );

        AiSuggestion saved =
                aiSuggestionRepository
                        .saveAndFlush(
                                suggestion
                        );

        aiPlanParserService
                .saveBodyAnalysisItems(
                        saved,
                        analysis
                );

        return saved;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiSuggestion markFailed(
            Long suggestionId,
            String errorCode,
            String errorMessage
    ) {
        AiSuggestion suggestion =
                getSuggestionForUpdate(
                        suggestionId
                );

        /*
         * Không cho lỗi đến muộn ghi đè
         * suggestion đã thành công hoặc đã apply.
         */
        if (
                suggestion.getStatus()
                        == AiSuggestionStatus.SUCCESS ||
                        suggestion.getStatus()
                                == AiSuggestionStatus.APPLIED
        ) {
            return suggestion;
        }

        /*
         * FAILED gọi lặp lại là idempotent.
         */
        if (
                suggestion.getStatus()
                        == AiSuggestionStatus.FAILED
        ) {
            return suggestion;
        }

        if (
                suggestion.getStatus()
                        != AiSuggestionStatus.PENDING
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        suggestion.markFailed(
                defaultText(
                        errorCode,
                        DEFAULT_FAILURE_CODE
                ),
                defaultText(
                        errorMessage,
                        DEFAULT_FAILURE_MESSAGE
                )
        );

        return aiSuggestionRepository
                .saveAndFlush(
                        suggestion
                );
    }

    // =====================================================
    // LOAD
    // =====================================================

    private AiSuggestion getPendingSuggestion(
            Long suggestionId,
            AiSuggestionType expectedType
    ) {
        AiSuggestion suggestion =
                getSuggestionForUpdate(
                        suggestionId
                );

        if (
                suggestion.getStatus()
                        != AiSuggestionStatus.PENDING
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                suggestion.getSuggestionType()
                        != expectedType
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return suggestion;
    }

    private AiSuggestion getSuggestionForUpdate(
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

        /*
         * Nếu repository hiện có:
         *
         * findByIdAndDeletedFalse(...)
         *
         * thì nên dùng method đó.
         *
         * Nếu chưa có thì tạm thời giữ findById().
         */
        AiSuggestion suggestion =
                aiSuggestionRepository
                        .findById(
                                suggestionId
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode
                                                .AI_SUGGESTION_NOT_FOUND
                                )
                        );

        if (
                Boolean.TRUE.equals(
                        suggestion.getDeleted()
                )
        ) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_NOT_FOUND
            );
        }

        return suggestion;
    }

    // =====================================================
    // SUCCESS
    // =====================================================

    private void applySuccessData(
            AiSuggestion suggestion,
            AiProviderResult providerResult,
            Object generatedResponse,
            String summary,
            String warningMessage
    ) {
        applyProviderMetadata(
                suggestion,
                providerResult
        );

        /*
         * Lưu normalized/generated JSON,
         * không lưu raw Gemini response vào field này.
         */
        suggestion.setAiResponse(
                toJson(
                        generatedResponse
                )
        );

        suggestion.setSummary(
                normalizeText(
                        summary
                )
        );

        suggestion.setWarningMessage(
                normalizeText(
                        warningMessage
                )
        );

        suggestion.markSuccess();
    }

    private void applyProviderMetadata(
            AiSuggestion suggestion,
            AiProviderResult providerResult
    ) {
        suggestion.setProvider(
                providerResult.getProvider()
        );

        suggestion.setModelName(
                normalizeText(
                        providerResult.getModelName()
                )
        );

        suggestion.setProviderRequestId(
                normalizeText(
                        providerResult
                                .getProviderRequestId()
                )
        );
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validatePendingSuggestion(
            AiSuggestion suggestion
    ) {
        if (suggestion == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                suggestion.getStatus()
                        != AiSuggestionStatus.PENDING
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                suggestion.getMember() == null ||
                        suggestion.getMember()
                                .getId() == null ||
                        suggestion.getSuggestionType() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                Boolean.TRUE.equals(
                        suggestion.getDeleted()
                )
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateProviderResult(
            AiProviderResult providerResult
    ) {
        if (
                providerResult == null ||
                        providerResult.getProvider() == null ||
                        providerResult.getRawResponse() == null ||
                        providerResult
                                .getRawResponse()
                                .isBlank()
        ) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    private void validateGeneratedResponse(
            Object generatedResponse
    ) {
        if (generatedResponse == null) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    // =====================================================
    // JSON / TEXT
    // =====================================================

    private String toJson(
            Object value
    ) {
        if (value == null) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        try {
            return objectMapper
                    .writeValueAsString(
                            value
                    );

        } catch (Exception exception) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    private String normalizeText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private String defaultText(
            String value,
            String defaultValue
    ) {
        String normalized =
                normalizeText(
                        value
                );

        return normalized == null
                ? defaultValue
                : normalized;
    }
}
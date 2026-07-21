package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutPlanResponse;
import com.fitlife.ai.entity.AiSuggestion;
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

    private final AiSuggestionRepository aiSuggestionRepository;
    private final AiPlanParserService aiPlanParserService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiSuggestion createPending(
            AiSuggestion suggestion
    ) {
        if (suggestion == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return aiSuggestionRepository.saveAndFlush(
                suggestion
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiSuggestion markFullPlanSuccess(
            Long suggestionId,
            AiProviderResult providerResult,
            AiGeneratedPlanResponse generatedPlan,
            String warningMessage
    ) {
        AiSuggestion suggestion =
                getSuggestionForUpdate(suggestionId);

        validateProviderResult(providerResult);

        if (generatedPlan == null) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        applyProviderMetadata(
                suggestion,
                providerResult
        );

        suggestion.setAiResponse(
                toJson(generatedPlan)
        );
        suggestion.setSummary(
                normalizeText(
                        generatedPlan.getSummary()
                )
        );
        suggestion.setWarningMessage(
                normalizeText(warningMessage)
        );
        suggestion.markSuccess();

        AiSuggestion savedSuggestion =
                aiSuggestionRepository.saveAndFlush(
                        suggestion
                );

        aiPlanParserService.savePlanItems(
                savedSuggestion,
                generatedPlan
        );

        return savedSuggestion;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiSuggestion markBodyAnalysisSuccess(
            Long suggestionId,
            AiProviderResult providerResult,
            AiGeneratedBodyAnalysisResponse analysis,
            String warningMessage
    ) {
        AiSuggestion suggestion =
                getSuggestionForUpdate(suggestionId);

        validateProviderResult(providerResult);

        if (analysis == null) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        applyProviderMetadata(
                suggestion,
                providerResult
        );

        suggestion.setAiResponse(
                toJson(analysis)
        );
        suggestion.setSummary(
                normalizeText(
                        analysis.getSummary()
                )
        );
        suggestion.setWarningMessage(
                normalizeText(warningMessage)
        );
        suggestion.markSuccess();

        AiSuggestion savedSuggestion =
                aiSuggestionRepository.saveAndFlush(
                        suggestion
                );

        aiPlanParserService.saveBodyAnalysisItems(
                savedSuggestion,
                analysis
        );

        return savedSuggestion;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiSuggestion markFailed(
            Long suggestionId,
            String errorCode,
            String errorMessage
    ) {
        AiSuggestion suggestion =
                getSuggestionForUpdate(suggestionId);

        suggestion.markFailed(
                defaultText(
                        errorCode,
                        "AI_REQUEST_FAILED"
                ),
                defaultText(
                        errorMessage,
                        "Không thể xử lý yêu cầu AI vào lúc này."
                )
        );

        return aiSuggestionRepository.saveAndFlush(
                suggestion
        );
    }

    private AiSuggestion getSuggestionForUpdate(
            Long suggestionId
    ) {
        if (suggestionId == null
                || suggestionId <= 0) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_NOT_FOUND
            );
        }

        return aiSuggestionRepository
                .findById(suggestionId)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.AI_SUGGESTION_NOT_FOUND
                        )
                );
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

    private void validateProviderResult(
            AiProviderResult providerResult
    ) {
        if (providerResult == null
                || providerResult.getProvider() == null
                || providerResult.getRawResponse() == null
                || providerResult
                .getRawResponse()
                .isBlank()) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    private String toJson(
            Object value
    ) {
        try {
            return objectMapper.writeValueAsString(
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

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private String defaultText(
            String value,
            String defaultValue
    ) {
        String normalized = normalizeText(value);

        return normalized == null
                ? defaultValue
                : normalized;
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
                getSuggestionForUpdate(suggestionId);

        validateProviderResult(providerResult);

        if (generated == null) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        applyProviderMetadata(
                suggestion,
                providerResult
        );

        suggestion.setAiResponse(
                toJson(generated)
        );

        suggestion.setSummary(
                normalizeText(
                        generated.getSummary()
                )
        );

        suggestion.setWarningMessage(
                normalizeText(warningMessage)
        );

        suggestion.markSuccess();

        AiSuggestion savedSuggestion =
                aiSuggestionRepository.saveAndFlush(
                        suggestion
                );

        aiPlanParserService.saveWorkoutPlanItems(
                savedSuggestion,
                generated
        );

        return savedSuggestion;
    }
}

package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.response.AiUsageTodayResponse;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.common.exception.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiUsageServiceImplTest {

    @Mock
    private AiSuggestionRepository aiSuggestionRepository;

    @InjectMocks
    private AiUsageServiceImpl aiUsageService;

    @Test
    void validateDailyLimit_shouldPass_whenUsageIsBelowLimit() {
        when(aiSuggestionRepository
                .countByMemberIdAndRequestedAtBetweenAndDeletedFalse(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(4L);

        aiUsageService.validateDailyLimit(1L);
    }

    @Test
    void validateDailyLimit_shouldThrow_whenUsageReachesLimit() {
        when(aiSuggestionRepository
                .countByMemberIdAndRequestedAtBetweenAndDeletedFalse(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(5L);

        assertThrows(
                AppException.class,
                () -> aiUsageService.validateDailyLimit(1L)
        );
    }

    @Test
    void validateDailyLimit_shouldThrow_whenMemberIdIsInvalid() {
        assertThrows(
                AppException.class,
                () -> aiUsageService.validateDailyLimit(null)
        );

        assertThrows(
                AppException.class,
                () -> aiUsageService.validateDailyLimit(0L)
        );
    }

    @Test
    void getTodayUsage_shouldReturnCorrectUsage() {
        when(aiSuggestionRepository
                .countByMemberIdAndRequestedAtBetweenAndDeletedFalse(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(2L);

        AiUsageTodayResponse response =
                aiUsageService.getTodayUsage(1L);

        assertEquals(5, response.getDailyLimit());
        assertEquals(2L, response.getUsed());
        assertEquals(3L, response.getRemaining());
        assertNotNull(response.getResetAt());
    }

    @Test
    void getTodayUsage_shouldNeverReturnNegativeRemaining() {
        when(aiSuggestionRepository
                .countByMemberIdAndRequestedAtBetweenAndDeletedFalse(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(7L);

        AiUsageTodayResponse response =
                aiUsageService.getTodayUsage(1L);

        assertEquals(0L, response.getRemaining());
    }
}

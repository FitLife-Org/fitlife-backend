package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.response.AiUsageTodayResponse;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiUsageServiceImplTest {

    @Mock
    private AiSuggestionRepository
            aiSuggestionRepository;

    private AiUsageServiceImpl aiUsageService;

    @BeforeEach
    void setUp() {
        aiUsageService =
                new AiUsageServiceImpl(
                        aiSuggestionRepository
                );
    }

    @Test
    void validateDailyLimit_shouldPass_whenUsageIsBelowLimit() {
        when(aiSuggestionRepository.countTodayUsage(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(4L);

        assertDoesNotThrow(
                () -> aiUsageService
                        .validateDailyLimit(1L)
        );

        verify(aiSuggestionRepository)
                .countTodayUsage(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void validateDailyLimit_shouldThrow_whenUsageReachesLimit() {
        when(aiSuggestionRepository.countTodayUsage(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(5L);

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> aiUsageService
                                .validateDailyLimit(1L)
                );

        assertEquals(
                ErrorCode.AI_LIMIT_EXCEEDED,
                exception.getErrorCode()
        );

        verify(aiSuggestionRepository)
                .countTodayUsage(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void validateDailyLimit_shouldThrow_whenUsageExceedsLimit() {
        when(aiSuggestionRepository.countTodayUsage(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(7L);

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> aiUsageService
                                .validateDailyLimit(1L)
                );

        assertEquals(
                ErrorCode.AI_LIMIT_EXCEEDED,
                exception.getErrorCode()
        );
    }

    @Test
    void validateDailyLimit_shouldThrow_whenMemberIdIsNull() {
        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> aiUsageService
                                .validateDailyLimit(null)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                aiSuggestionRepository
        );
    }

    @Test
    void validateDailyLimit_shouldThrow_whenMemberIdIsZero() {
        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> aiUsageService
                                .validateDailyLimit(0L)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                aiSuggestionRepository
        );
    }

    @Test
    void validateDailyLimit_shouldThrow_whenMemberIdIsNegative() {
        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> aiUsageService
                                .validateDailyLimit(-1L)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                aiSuggestionRepository
        );
    }

    @Test
    void getTodayUsage_shouldReturnCorrectUsage() {
        when(aiSuggestionRepository.countTodayUsage(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(2L);

        AiUsageTodayResponse response =
                aiUsageService.getTodayUsage(1L);

        assertEquals(
                LocalDate.now(),
                response.getDate()
        );

        assertEquals(
                5,
                response.getDailyLimit()
        );

        assertEquals(
                2L,
                response.getUsed()
        );

        assertEquals(
                3L,
                response.getRemaining()
        );

        assertFalse(
                response.isLimitReached()
        );

        assertNotNull(
                response.getResetAt()
        );

        assertEquals(
                LocalDate.now()
                        .plusDays(1)
                        .atStartOfDay(),
                response.getResetAt()
        );
    }

    @Test
    void getTodayUsage_shouldMarkLimitReached_whenUsageEqualsLimit() {
        when(aiSuggestionRepository.countTodayUsage(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(5L);

        AiUsageTodayResponse response =
                aiUsageService.getTodayUsage(1L);

        assertEquals(
                5L,
                response.getUsed()
        );

        assertEquals(
                0L,
                response.getRemaining()
        );

        assertTrue(
                response.isLimitReached()
        );
    }

    @Test
    void getTodayUsage_shouldNeverReturnNegativeRemaining() {
        when(aiSuggestionRepository.countTodayUsage(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(7L);

        AiUsageTodayResponse response =
                aiUsageService.getTodayUsage(1L);

        assertEquals(
                7L,
                response.getUsed()
        );

        assertEquals(
                0L,
                response.getRemaining()
        );

        assertTrue(
                response.isLimitReached()
        );
    }

    @Test
    void getTodayUsage_shouldReturnZeroUsage() {
        when(aiSuggestionRepository.countTodayUsage(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(0L);

        AiUsageTodayResponse response =
                aiUsageService.getTodayUsage(1L);

        assertEquals(
                0L,
                response.getUsed()
        );

        assertEquals(
                5L,
                response.getRemaining()
        );

        assertFalse(
                response.isLimitReached()
        );
    }

    @Test
    void getTodayUsage_shouldRejectInvalidMemberId() {
        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> aiUsageService
                                .getTodayUsage(null)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verify(
                aiSuggestionRepository,
                never()
        ).countTodayUsage(
                any(),
                any(),
                any()
        );
    }
}
package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.response.AiUsageTodayResponse;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.ai.service.AiUsageService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiUsageServiceImpl
        implements AiUsageService {

    private static final int DAILY_AI_LIMIT = 10;

    private static final ZoneId FITLIFE_ZONE_ID =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final AiSuggestionRepository
            aiSuggestionRepository;

    @Override
    public void validateDailyLimit(Long memberId) {
        validateMemberId(memberId);

        UsageWindow usageWindow =
                createTodayUsageWindow();

        long used = countUsage(
                memberId,
                usageWindow
        );

        if (used >= DAILY_AI_LIMIT) {
            throw new AppException(
                    ErrorCode.AI_LIMIT_EXCEEDED
            );
        }
    }

    @Override
    public AiUsageTodayResponse getTodayUsage(
            Long memberId
    ) {
        validateMemberId(memberId);

        UsageWindow usageWindow =
                createTodayUsageWindow();

        long used = countUsage(
                memberId,
                usageWindow
        );

        long remaining = Math.max(
                0L,
                DAILY_AI_LIMIT - used
        );

        return AiUsageTodayResponse.builder()
                .date(usageWindow.date())
                .dailyLimit(DAILY_AI_LIMIT)
                .used(used)
                .remaining(remaining)
                .limitReached(
                        used >= DAILY_AI_LIMIT
                )
                .resetAt(usageWindow.to())
                .build();
    }

    private long countUsage(
            Long memberId,
            UsageWindow usageWindow
    ) {
        return aiSuggestionRepository
                .countTodayUsage(
                        memberId,
                        usageWindow.from(),
                        usageWindow.to()
                );
    }

    private UsageWindow createTodayUsageWindow() {
        LocalDate today =
                LocalDate.now(FITLIFE_ZONE_ID);

        LocalDateTime from =
                today.atStartOfDay();

        LocalDateTime to =
                today.plusDays(1)
                        .atStartOfDay();

        return new UsageWindow(
                today,
                from,
                to
        );
    }

    private void validateMemberId(
            Long memberId
    ) {
        if (memberId == null || memberId <= 0) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private record UsageWindow(
            LocalDate date,
            LocalDateTime from,
            LocalDateTime to
    ) {
    }
}
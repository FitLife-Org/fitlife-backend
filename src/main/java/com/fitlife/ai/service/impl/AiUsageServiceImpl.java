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
public class AiUsageServiceImpl implements AiUsageService {

    private static final int DAILY_AI_LIMIT = 5;

    private static final ZoneId FITLIFE_ZONE_ID =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final AiSuggestionRepository aiSuggestionRepository;

    @Override
    @Transactional(readOnly = true)
    public void validateDailyLimit(Long memberId) {
        validateMemberId(memberId);

        long used = countTodayUsage(memberId);

        if (used >= DAILY_AI_LIMIT) {
            throw new AppException(
                    ErrorCode.AI_LIMIT_EXCEEDED
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AiUsageTodayResponse getTodayUsage(
            Long memberId
    ) {
        validateMemberId(memberId);

        UsageWindow usageWindow =
                createTodayUsageWindow();

        long used = aiSuggestionRepository
                .countByMemberIdAndRequestedAtBetweenAndDeletedFalse(
                        memberId,
                        usageWindow.from(),
                        usageWindow.to()
                );

        long remaining = Math.max(
                0L,
                DAILY_AI_LIMIT - used
        );

        return AiUsageTodayResponse.builder()
                .dailyLimit(DAILY_AI_LIMIT)
                .used(used)
                .remaining(remaining)
                .resetAt(usageWindow.to())
                .build();
    }

    private long countTodayUsage(
            Long memberId
    ) {
        UsageWindow usageWindow =
                createTodayUsageWindow();

        return aiSuggestionRepository
                .countByMemberIdAndRequestedAtBetweenAndDeletedFalse(
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
                today.plusDays(1).atStartOfDay();

        return new UsageWindow(from, to);
    }

    private void validateMemberId(
            Long memberId
    ) {
        if (memberId == null || memberId <= 0) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }
    }

    private record UsageWindow(
            LocalDateTime from,
            LocalDateTime to
    ) {
    }
}

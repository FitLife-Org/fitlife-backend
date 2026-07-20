package com.fitlife.ai.service;

import com.fitlife.ai.dto.response.AiUsageTodayResponse;

/**
 * Quản lý giới hạn và thống kê lượt sử dụng AI.
 */
public interface AiUsageService {

    /**
     * Kiểm tra Member còn lượt sử dụng AI trong ngày hay không.
     *
     * @param memberId ID của Member
     */
    void validateDailyLimit(Long memberId);

    /**
     * Lấy thống kê lượt sử dụng AI của Member trong ngày hiện tại.
     *
     * @param memberId ID của Member
     * @return thông tin giới hạn, số lượt đã dùng, còn lại và thời điểm reset
     */
    AiUsageTodayResponse getTodayUsage(Long memberId);
}
package com.fitlife.ai.service;

import com.fitlife.ai.dto.response.AiUsageTodayResponse;

/**
 * Quản lý quota và thống kê lượt sử dụng AI của Member.
 *
 * Quota được tính theo ngày tại múi giờ Asia/Ho_Chi_Minh.
 */
public interface AiUsageService {

    /**
     * Kiểm tra Member còn lượt sử dụng AI trong ngày hay không.
     *
     * @param memberId ID của Member
     * @throws com.fitlife.common.exception.AppException
     *         khi memberId không hợp lệ hoặc quota đã hết
     */
    void validateDailyLimit(Long memberId);

    /**
     * Lấy thống kê sử dụng AI của Member trong ngày hiện tại.
     *
     * @param memberId ID của Member
     * @return thống kê quota trong ngày
     */
    AiUsageTodayResponse getTodayUsage(Long memberId);
}
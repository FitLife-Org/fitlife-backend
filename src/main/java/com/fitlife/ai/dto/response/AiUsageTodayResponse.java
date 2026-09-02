package com.fitlife.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AiUsageTodayResponse {

    /**
     * Ngày đang được thống kê theo múi giờ FitLife.
     */
    private final LocalDate date;

    /**
     * Giới hạn lượt AI mỗi ngày.
     */
    private final int dailyLimit;

    /**
     * Tổng số lượt đã sử dụng trong ngày.
     */
    private final long used;

    /**
     * Tổng số lượt còn lại.
     */
    private final long remaining;

    /**
     * true nếu đã dùng hết hoặc vượt giới hạn.
     */
    private final boolean limitReached;

    /**
     * Thời điểm quota được reset.
     */
    private final LocalDateTime resetAt;
}
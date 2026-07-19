package com.fitlife.ai.enums;

/**
 * Mức độ vận động tổng thể của hội viên.
 *
 * Giá trị này được gửi vào prompt AI để ước lượng
 * mức vận động và xây dựng kế hoạch phù hợp.
 */
public enum ActivityLevel {

    /**
     * Ít vận động, phần lớn thời gian ngồi.
     */
    SEDENTARY,

    /**
     * Vận động nhẹ khoảng 1–2 ngày mỗi tuần.
     */
    LIGHT,

    /**
     * Vận động vừa khoảng 3–4 ngày mỗi tuần.
     */
    MODERATE,

    /**
     * Vận động thường xuyên khoảng 5–6 ngày mỗi tuần.
     */
    ACTIVE,

    /**
     * Vận động cường độ cao hoặc lao động thể chất thường xuyên.
     */
    VERY_ACTIVE
}
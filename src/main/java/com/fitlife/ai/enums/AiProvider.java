package com.fitlife.ai.enums;

/**
 * Nhà cung cấp mô hình AI được sử dụng để tạo suggestion.
 *
 * Thiết kế dưới dạng enum giúp:
 * - Không lưu sai tên provider.
 * - Dễ mở rộng thêm provider trong tương lai.
 * - Đồng bộ dữ liệu database và mã nguồn.
 */
public enum AiProvider {

    /**
     * Google Gemini API.
     */
    GEMINI

}
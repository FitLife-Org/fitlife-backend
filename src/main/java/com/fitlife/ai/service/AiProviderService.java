package com.fitlife.ai.service;

import com.fitlife.ai.dto.internal.AiProviderResult;

/**
 * Contract chung cho các AI provider.
 */
public interface AiProviderService {

    /**
     * Gửi prompt đến provider và trả về kết quả đã chuẩn hóa.
     *
     * @param prompt prompt hoàn chỉnh
     * @return provider result
     */
    AiProviderResult generate(String prompt);
}
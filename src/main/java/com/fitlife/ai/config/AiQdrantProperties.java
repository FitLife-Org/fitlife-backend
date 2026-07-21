package com.fitlife.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "fitlife.ai.qdrant")
public class AiQdrantProperties {

    /**
     * Bật/tắt toàn bộ tích hợp Qdrant.
     */
    private boolean enabled = false;

    /**
     * Tự kiểm tra và tạo collection khi ứng dụng khởi động.
     */
    private boolean initializeCollection = true;

    /**
     * Nếu true, ứng dụng dừng khởi động khi Qdrant lỗi hoặc collection sai cấu hình.
     */
    private boolean failFast = true;

    /**
     * URL REST API của Qdrant.
     */
    private String baseUrl = "http://localhost:6333";

    /**
     * API key dùng cho Qdrant Cloud hoặc self-hosted có bật xác thực.
     * Để trống khi chạy local không bật API key.
     */
    private String apiKey;

    /**
     * Một collection duy nhất cho knowledge base MVP.
     */
    private String collectionName = "fitlife_knowledge";

    /**
     * Phải khớp chính xác số chiều vector từ embedding model ở AI-BE-21.
     */
    private int vectorSize = 768;

    /**
     * Cosine phù hợp với phần lớn text embedding đã chuẩn hóa.
     */
    private String distance = "Cosine";

    private boolean onDiskPayload = true;

    private Duration connectTimeout = Duration.ofSeconds(5);

    private Duration readTimeout = Duration.ofSeconds(15);

    public void validate() {
        if (!enabled) {
            return;
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "fitlife.ai.qdrant.base-url must not be blank"
            );
        }

        if (collectionName == null || collectionName.isBlank()) {
            throw new IllegalStateException(
                    "fitlife.ai.qdrant.collection-name must not be blank"
            );
        }

        if (vectorSize <= 0) {
            throw new IllegalStateException(
                    "fitlife.ai.qdrant.vector-size must be greater than 0"
            );
        }

        if (!"Cosine".equalsIgnoreCase(distance)
                && !"Dot".equalsIgnoreCase(distance)
                && !"Euclid".equalsIgnoreCase(distance)
                && !"Manhattan".equalsIgnoreCase(distance)) {
            throw new IllegalStateException(
                    "Unsupported Qdrant distance: " + distance
            );
        }

        if (connectTimeout == null || connectTimeout.isNegative()
                || connectTimeout.isZero()) {
            throw new IllegalStateException(
                    "fitlife.ai.qdrant.connect-timeout must be positive"
            );
        }

        if (readTimeout == null || readTimeout.isNegative()
                || readTimeout.isZero()) {
            throw new IllegalStateException(
                    "fitlife.ai.qdrant.read-timeout must be positive"
            );
        }
    }

    public String normalizedBaseUrl() {
        String value = baseUrl == null ? "" : baseUrl.trim();

        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }

    public String normalizedDistance() {
        if ("cosine".equalsIgnoreCase(distance)) {
            return "Cosine";
        }

        if ("dot".equalsIgnoreCase(distance)) {
            return "Dot";
        }

        if ("euclid".equalsIgnoreCase(distance)) {
            return "Euclid";
        }

        if ("manhattan".equalsIgnoreCase(distance)) {
            return "Manhattan";
        }

        return distance;
    }
}

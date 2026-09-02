package com.fitlife.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "ApiResponse",
        description = "Wrapper phản hồi chuẩn của FitLife API"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    public static final int SUCCESS_CODE = 200;
    public static final int CREATED_CODE = 201;

    @Builder.Default
    @Schema(
            description = "Mã kết quả nội bộ của API",
            example = "200"
    )
    private int code = SUCCESS_CODE;

    @Builder.Default
    @Schema(
            description = "Thông điệp mô tả kết quả xử lý",
            example = "Success"
    )
    private String message = "Success";

    @Schema(
            description = "Dữ liệu trả về theo từng endpoint",
            nullable = true
    )
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return build(
                SUCCESS_CODE,
                "Success",
                data
        );
    }

    public static <T> ApiResponse<T> success(
            String message,
            T data
    ) {
        return build(
                SUCCESS_CODE,
                normalizeMessage(message, "Success"),
                data
        );
    }

    public static <T> ApiResponse<T> success(
            String message
    ) {
        return build(
                SUCCESS_CODE,
                normalizeMessage(message, "Success"),
                null
        );
    }

    public static <T> ApiResponse<T> created(T data) {
        return build(
                CREATED_CODE,
                "Created successfully",
                data
        );
    }

    public static <T> ApiResponse<T> created(
            String message,
            T data
    ) {
        return build(
                CREATED_CODE,
                normalizeMessage(
                        message,
                        "Created successfully"
                ),
                data
        );
    }

    public static <T> ApiResponse<T> error(
            int code,
            String message
    ) {
        return build(
                code,
                normalizeMessage(message, "Error"),
                null
        );
    }

    public static <T> ApiResponse<T> error(
            int code,
            String message,
            T data
    ) {
        return build(
                code,
                normalizeMessage(message, "Error"),
                data
        );
    }

    private static <T> ApiResponse<T> build(
            int code,
            String message,
            T data
    ) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    private static String normalizeMessage(
            String message,
            String defaultMessage
    ) {
        return message == null || message.isBlank()
                ? defaultMessage
                : message;
    }
}

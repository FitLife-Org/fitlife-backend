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

    @Builder.Default
    @Schema(
            description = "Mã kết quả nội bộ của API",
            example = "200"
    )
    private int code = 200;

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

    public static <T> ApiResponse<T> success(
            T data
    ) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(
            String message,
            T data
    ) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(
            String message
    ) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> created(
            T data
    ) {
        return ApiResponse.<T>builder()
                .code(201)
                .message("Created successfully")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(
            String message,
            T data
    ) {
        return ApiResponse.<T>builder()
                .code(201)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(
            int code,
            String message
    ) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(
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
}
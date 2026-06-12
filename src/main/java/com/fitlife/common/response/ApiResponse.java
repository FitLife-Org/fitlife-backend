package com.fitlife.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Schema(name = "ApiResponse", description = "Wrapper pháº£n há»“i chuáº©n cá»§a FitLife API")
// If data is null, it won't be included in the JSON response
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Schema(description = "MĂ£ tráº¡ng thĂ¡i ná»™i bá»™ cá»§a API", example = "200")
    private int code;

    @Schema(description = "ThĂ´ng Ä‘iá»‡p mĂ´ táº£ káº¿t quáº£ xá»­ lĂ½", example = "Success")
    private String message;

    @Schema(description = "Dá»¯ liá»‡u tráº£ vá» theo tá»«ng endpoint", nullable = true)
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String customMessage) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(customMessage)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .code(201)
                .message(message)
                .data(data)
                .build();
    }
}

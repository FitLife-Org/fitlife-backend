package com.fitlife.common.response;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.fitlife")
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> parameterType = returnType.getParameterType();
        
        // Bỏ qua nếu dữ liệu đã là ApiResponse
        if (ApiResponse.class.isAssignableFrom(parameterType)) {
            return false;
        }
        
        // Bỏ qua các định dạng file, mảng byte (ví dụ: download file)
        if (Resource.class.isAssignableFrom(parameterType) || byte[].class.isAssignableFrom(parameterType)) {
            return false;
        }

        // Bỏ qua String để tránh ClassCastException với StringHttpMessageConverter
        if (String.class.isAssignableFrom(parameterType)) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        
        // Nếu body đã là ApiResponse (vd: từ GlobalExceptionHandler), trả về trực tiếp
        if (body instanceof ApiResponse) {
            return body;
        }

        // Nếu body null, trả về success với data null
        if (body == null) {
            return ApiResponse.success(null);
        }

        // Nếu không, bọc dữ liệu trong ApiResponse
        return ApiResponse.success(body);
    }
}

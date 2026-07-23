package com.fitlife.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        ErrorCode errorCode =
                ErrorCode.FORBIDDEN;

        ApiResponse<Void> body =
                ApiResponse.error(
                        errorCode.getCode(),
                        errorCode.getMessage()
                );

        response.setStatus(
                errorCode.getHttpStatus().value()
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                body
        );
    }
}
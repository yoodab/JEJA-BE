package com.jeja.jejabe.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeja.jejabe.global.response.ApiResponseForm;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("[AccessDeniedHandler] 권한 부족: {}", accessDeniedException.getMessage());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorCode errorCode = CommonErrorCode.FORBIDDEN;
        ApiResponseForm<Void> errorResponse = ApiResponseForm.error(errorCode.getCode(), errorCode.getMessage());

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}

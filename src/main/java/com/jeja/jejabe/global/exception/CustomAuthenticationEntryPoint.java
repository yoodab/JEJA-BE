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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        log.error("[AuthenticationEntryPoint] 인증 실패: {}", authException.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorCode errorCode = CommonErrorCode.LOGIN_REQUIRED;
        ApiResponseForm<Void> errorResponse = ApiResponseForm.error(errorCode.getCode(), errorCode.getMessage());

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}

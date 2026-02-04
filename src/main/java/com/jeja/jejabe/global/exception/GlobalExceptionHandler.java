package com.jeja.jejabe.global.exception;

import com.jeja.jejabe.global.response.ApiResponseForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice // 모든 @RestController에서 발생하는 예외를 잡아 처리
public class GlobalExceptionHandler {

    // 우리가 직접 정의한 GeneralException 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleGeneralException(GeneralException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[GeneralException] code: {}, message: {}", errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseForm.error(errorCode.getCode(), errorCode.getMessage()));
    }

    // @Valid 검증 실패 시 발생하는 예외 처리
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleMethodArgumentNotValidException(org.springframework.web.bind.MethodArgumentNotValidException e) {
        log.warn("[MethodArgumentNotValidException] ", e);
        // 첫 번째 에러 메시지만 반환하거나, 전체를 합쳐서 반환
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorCode errorCode = CommonErrorCode.BAD_REQUEST;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseForm.error(errorCode.getCode(), errorMessage));
    }

    // 권한 없음 예외 처리 (Security Filter 이후 Controller 진입 전/후 발생 가능)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e) {
        log.warn("[AccessDeniedException] ", e);
        ErrorCode errorCode = CommonErrorCode.FORBIDDEN;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseForm.error(errorCode.getCode(), errorCode.getMessage()));
    }

    // 지원하지 않는 HTTP 메서드 요청 시
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleHttpRequestMethodNotSupportedException(org.springframework.web.HttpRequestMethodNotSupportedException e) {
        log.warn("[HttpRequestMethodNotSupportedException] ", e);
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponseForm.error("C005", "지원하지 않는 HTTP 메서드입니다."));
    }

    // 기타 예상치 못한 모든 RuntimeException 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleRuntimeException(RuntimeException e) {
        log.error("[RuntimeException]", e); // 예상치 못한 에러는 스택 트레이스를 모두 기록
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseForm.error(errorCode.getCode(), errorCode.getMessage()));
    }

    // 그 외 모든 Exception 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseForm<Void>> handleException(Exception e) {
        log.error("[Exception]", e);
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseForm.error(errorCode.getCode(), errorCode.getMessage()));
    }
}

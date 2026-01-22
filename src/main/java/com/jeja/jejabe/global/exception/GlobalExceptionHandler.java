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

    // 기타 예상치 못한 모든 RuntimeException 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleRuntimeException(RuntimeException e) {
        log.error("[RuntimeException]", e); // 예상치 못한 에러는 스택 트레이스를 모두 기록
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseForm.error(errorCode.getCode(), errorCode.getMessage()));
    }
}

package com.jeja.jejabe.auth.dto;

import lombok.Data;

@Data
public class PasswordResetRequestDto {
    private String email;
    private String authCode; // 보안을 위해 변경 시에도 인증코드 재확인
    private String newPassword;
}

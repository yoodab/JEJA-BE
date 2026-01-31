package com.jeja.jejabe.auth.dto;

import lombok.Data;

@Data
public class VerificationCodeRequestDto {
    private String loginId;
    private String email;
}

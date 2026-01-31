package com.jeja.jejabe.auth.dto;

import lombok.Data;

@Data
public class VerificationCodeCheckDto {
    private String email;
    private String authCode;
}

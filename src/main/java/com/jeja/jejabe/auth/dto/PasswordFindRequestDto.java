package com.jeja.jejabe.auth.dto;

import lombok.Data;

@Data
public class PasswordFindRequestDto {
    private String loginId;
    private String email;
}
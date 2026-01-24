package com.jeja.jejabe.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SignupRequestDto {
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private LocalDate birthDate;
}

package com.jeja.jejabe.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private String birthDate;
}

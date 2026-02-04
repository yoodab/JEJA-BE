package com.jeja.jejabe.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenReissueRequestDto {
    private String refreshToken;
}

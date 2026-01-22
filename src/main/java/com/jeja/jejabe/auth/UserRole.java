package com.jeja.jejabe.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ROLE_USER("일반 사용자"),
    ROLE_ADMIN("관리자"),
    ROLE_PASTOR("교역자"); // 필요하다면

    private final String description;
}

package com.jeja.jejabe.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    PENDING("승인 대기"),
    ACTIVE("활성"),
    INACTIVE("비활성");

    private final String description;
}

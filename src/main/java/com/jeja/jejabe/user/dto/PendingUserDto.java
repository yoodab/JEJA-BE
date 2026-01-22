package com.jeja.jejabe.user.dto;

import com.jeja.jejabe.auth.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PendingUserDto {
    private final Long userId;
    private final String loginId;
    private final String name; // Member 정보
    private final String phone; // Member 정보
    private final LocalDateTime createdAt;

    public PendingUserDto(User user) {
        this.userId = user.getId();
        this.loginId = user.getLoginId();
        this.name = user.getMember().getName();
        this.phone = user.getMember().getPhone();
        this.createdAt = user.getCreatedAt();
    }
}

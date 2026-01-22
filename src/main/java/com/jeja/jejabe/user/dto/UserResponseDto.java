package com.jeja.jejabe.user.dto;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.auth.UserStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String loginId;
    private String name;
    private String phone;
    private UserStatus status;
    private LocalDateTime createdAt;

    public UserResponseDto(User user) {
        this.userId = user.getId();
        this.loginId = user.getLoginId();
        this.name = user.getMember().getName();
        this.phone = user.getMember().getPhone();
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
    }
}
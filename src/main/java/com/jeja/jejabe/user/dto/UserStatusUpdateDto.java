package com.jeja.jejabe.user.dto;

import com.jeja.jejabe.auth.UserStatus;
import lombok.Data;

@Data
public class UserStatusUpdateDto {
    private UserStatus status; // JSON 예시: { "status": "ACTIVE" }
}

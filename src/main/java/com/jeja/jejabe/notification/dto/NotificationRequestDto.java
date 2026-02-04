package com.jeja.jejabe.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationRequestDto {
    private Long targetUserId;
    private String title;
    private String body;
}

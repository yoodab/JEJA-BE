package com.jeja.jejabe.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NotificationSendRequestDto {
    private TargetType targetType;
    private List<Long> targetMemberIds; // Member ID 목록
    private String title;
    private String body;

    public enum TargetType {
        USER,
        SOONJANG,
        ALL
    }
}

package com.jeja.jejabe.schedule.domain;

public enum RecurrenceRule {
    NONE,    // 반복 없음
    DAILY,   // 매일
    WEEKLY,  // 매주
    WEEKLY_DAYS, // 특정 요일 반복
    MONTHLY, // 매월
    YEARLY   // 매년
}
package com.jeja.jejabe.form.domain;

public enum QuestionType {
    SHORT_TEXT,     // 주관식 단답
    LONG_TEXT,      // 주관식 장문
    SINGLE_CHOICE,  // 객관식 (라디오)
    MULTIPLE_CHOICE,// 체크박스
    NUMBER,         // 숫자
    BOOLEAN,        // 참/거짓 (스위치/체크박스)
    WORSHIP_ATTENDANCE, // 예배 출석
    SCHEDULE_ATTENDANCE // 일정 출석
}

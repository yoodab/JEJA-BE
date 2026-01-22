package com.jeja.jejabe.schedule.domain;

public enum SharingScope {
    PUBLIC,         // 전체 공개
    LOGGED_IN_USERS, // 로그인한 사용자에게만 공개
    PRIVATE         // 비공개 (관리자)
}
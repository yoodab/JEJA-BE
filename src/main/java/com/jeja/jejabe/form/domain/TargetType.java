package com.jeja.jejabe.form.domain;

public enum TargetType {
    ALL,    // 로그인한 모든 사용자
    ROLE,   // 특정 직분/권한
    USER,   // 특정 회원
    CLUB,   // 특정 팀/동아리
    GUEST   // 비로그인 포함 누구나
}

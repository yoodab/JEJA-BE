package com.jeja.jejabe.club;

public enum ClubType {
    // 🔥 시스템 핵심 부서 (API 권한 등 특수 기능과 매핑됨)
    NEW_BELIEVER("새신자관리팀"), // 새신자 등록/관리 권한
    WORSHIP("찬양팀"),           // 콘티/악보 관리 권한
    BROADCAST("방송팀"),         // 방송 장비/스케줄 관리
    CONTENT("컨텐츠팀"),
    DESIGN("디자인팀"),
    SERVICE("예배팀"),

    // 🍀 나중에 유저들이 자유롭게 만들 동아리
    HOBBY("취미/친목");         // 별도 특수 권한 없음 (게시판, 일정 등 기본 기능만)

    private final String description;

    ClubType(String description) {
        this.description = description;
    }
}
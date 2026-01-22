package com.jeja.jejabe.album;

public enum PermissionType {
    // 보기 권한
    PUBLIC_READ,         // 전체 공개
    MEMBERS_ONLY_READ,   // 로그인한 사용자만
    ADMIN_ONLY_READ,     // 관리자 그룹만 (임원, 목사님, 관리자)

    // 쓰기 권한
    MEMBERS_WRITE, // (향후 확장 가능) 로그인한 사용자 쓰기
    ADMIN_WRITE      // 관리자 그룹만 쓰기
}

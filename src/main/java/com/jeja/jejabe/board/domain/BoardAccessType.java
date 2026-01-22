package com.jeja.jejabe.board.domain;

public enum BoardAccessType {
    PUBLIC,         // 전체 공개 (비로그인 가능)
    MEMBER,         // 로그인한 모든 회원
    CLUB,           // 특정 클럽(부서/팀) 멤버만
    ADMIN           // 관리자만
}

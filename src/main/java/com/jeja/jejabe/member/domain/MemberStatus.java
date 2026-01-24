package com.jeja.jejabe.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {
    NEWCOMER("새신자"),
    ACTIVE("재적"),
    LONG_TERM_ABSENT("장결자"),
    MOVED("교회 이동"),
    GRADUATED("졸업"),
    INACTIVE("비활성"),
    SYSTEM("시스템");

    private final String description;
}

package com.jeja.jejabe.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    MEMBER("일반성도"),
    CELL_LEADER("순장"),
    CELL_SUB_LEADER("부순장"),
    TEAM_LEADER("팀장"),
    EXECUTIVE("임원");

    private final String description;
}

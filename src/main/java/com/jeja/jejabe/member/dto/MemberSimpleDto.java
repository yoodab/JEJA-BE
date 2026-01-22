package com.jeja.jejabe.member.dto;

import com.jeja.jejabe.member.domain.Member;
import lombok.Getter;

@Getter
public class MemberSimpleDto {
    private final Long memberId;
    private final String name;
    // ... 셀 정보 등 필요한 최소 정보 ...

    public MemberSimpleDto(Member member) {
        this.memberId = member.getId();
        this.name = member.getName();
    }
}

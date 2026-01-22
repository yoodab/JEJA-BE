package com.jeja.jejabe.cell.dto;

import com.jeja.jejabe.member.domain.Member;
import lombok.Getter;

@Getter
public class MemberSimpleDto {
    private final Long memberId;
    private final String name;

    public MemberSimpleDto(Member member) {
        this.memberId = member.getId();
        this.name = member.getName();
    }
}

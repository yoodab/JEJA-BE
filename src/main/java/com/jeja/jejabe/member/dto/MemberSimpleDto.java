package com.jeja.jejabe.member.dto;

import com.jeja.jejabe.member.domain.Member;
import lombok.Getter;

@Getter
public class MemberSimpleDto {
    private final Long memberId;
    private final String name;
    private final String phone;
    private final java.time.LocalDate birthDate;

    public MemberSimpleDto(Member member) {
        this.memberId = member.getId();
        this.name = member.getName();
        this.phone = member.getPhone();
        this.birthDate = member.getBirthDate();
    }
}

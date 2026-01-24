package com.jeja.jejabe.member.dto;

import com.jeja.jejabe.member.domain.Gender;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.member.domain.MemberStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
public class MemberDto {
    private final Long memberId;
    private final String name;
    private final String phone;
    private final LocalDate birthDate;
    private final MemberStatus memberStatus;
    private final String memberImageUrl;
    private final Set<MemberRole> roles;
    private final boolean hasAccount;
    private final String gender;
    private final int age;

    public MemberDto(Member member) {
        this.memberId = member.getId();
        this.name = member.getName();
        this.phone = member.getPhone();
        this.birthDate = member.getBirthDate();
        this.memberStatus = member.getMemberStatus();
        this.hasAccount = (member.getUser() != null);
        this.roles = new HashSet<>(member.getRoles());
        this.memberImageUrl = member.getMemberImageUrl();
        this.gender = (member.getGender() != null) ? member.getGender().getDescription() : null;
        this.age = calculateAge(member.getBirthDate());
    }

    private int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;

        int birthYear = birthDate.getYear();
        int currentYear = LocalDate.now().getYear();

        // 한국 나이 (연 나이): 현재연도 - 출생연도 + 1
        return currentYear - birthYear + 1;
    }
}

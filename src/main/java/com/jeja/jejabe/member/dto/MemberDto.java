package com.jeja.jejabe.member.dto;

import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.member.domain.MemberStatus;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class MemberDto {
    private final Long memberId;
    private final String name;
    private final String phone;
    private final String birthDate;
    private final MemberStatus memberStatus;
    private final String memberImageUrl;
    private final Set<MemberRole> roles;
    private final boolean hasAccount; // 웹 계정 등록 여부
    private final String gender; // "남성" or "여성"
    private final int age;       // 만 나이 계산

    public MemberDto(Member member) {
        this.memberId = member.getId();
        this.name = member.getName();
        this.phone = member.getPhone();
        this.birthDate = member.getBirthDate();
        this.memberStatus = member.getMemberStatus();
        this.hasAccount = (member.getUser() != null);
        this.roles = new HashSet<>(member.getRoles());
        this.memberImageUrl = member.getMemberImageUrl();
        this.gender = member.getGender();
        // 생년월일(String YYYY-MM-DD)로 나이 계산 로직 (간단 버전)
        this.age = calculateAge(member.getBirthDate());
    }

    private int calculateAge(String birthDate) {
        if (birthDate == null || birthDate.length() < 4) return 0;
        try {
            int birthYear = Integer.parseInt(birthDate.substring(0, 4));
            return java.time.LocalDate.now().getYear() - birthYear + 1; // 한국 나이 or 만 나이
        } catch (NumberFormatException e) { return 0; }
    }
}

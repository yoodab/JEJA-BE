package com.jeja.jejabe.member.dto;

import com.jeja.jejabe.member.domain.Gender;
import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.member.domain.MemberStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class MemberCreateRequestDto {
    private String name;
    private String phone;
    private LocalDate birthDate;
    private Gender gender;
    private MemberStatus memberStatus;
    private String memberImageUrl;

    private Set<MemberRole> roles;
}

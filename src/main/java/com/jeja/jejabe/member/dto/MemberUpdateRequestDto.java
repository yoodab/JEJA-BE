package com.jeja.jejabe.member.dto;

import com.jeja.jejabe.member.domain.MemberRole;
import com.jeja.jejabe.member.domain.MemberStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class MemberUpdateRequestDto {
    private String name;
    private String phone;
    private LocalDate birthDate;
    private MemberStatus memberStatus;
    private Set<MemberRole> roles;
    private String memberImageUrl;
}

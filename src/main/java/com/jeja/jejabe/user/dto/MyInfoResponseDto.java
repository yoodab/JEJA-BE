package com.jeja.jejabe.user.dto;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.cell.Cell;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.member.domain.MemberRole;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
public class MyInfoResponseDto {
    private Long userId;
    private String loginId;
    private String name;
    private String phone;
    private String birthDate;

    private String role;
    private String status;

    private Long soonId;
    private String soonName;

    private boolean hasAccount;

    private Set<MemberRole> memberRoles;

    @Builder
    private MyInfoResponseDto(Long userId, String loginId, String name, String phone, String birthDate, String role, String status, String position, Long soonId, String soonName, boolean hasAccount, Set<MemberRole> memberRoles) {
        this.userId = userId;
        this.loginId = loginId;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.role = role;
        this.status = status;
        this.soonId = soonId;
        this.soonName = soonName;
        this.memberRoles= memberRoles;
        this.hasAccount = hasAccount;
    }

    // 1. [관리자용] Member 정보가 없을 때 생성하는 메소드
    public static MyInfoResponseDto fromAdmin(User user) {
        return MyInfoResponseDto.builder()
                .userId(null) // Member ID 없음
                .loginId(user.getLoginId())
                .name("관리자") // 또는 user.getLoginId()
                .phone(null)
                .birthDate(null)
                .role(user.getUserRole().name())
                .status("ACTIVE") // 시스템 상 활성 상태
                .soonId(null)
                .soonName(null)
                .hasAccount(true)
                .memberRoles(null)
                .build();
    }

    // 2. [일반 회원용] Member 및 Cell 정보가 있을 때 생성하는 메소드
    public static MyInfoResponseDto fromMember(User user, Member member, Cell cell) {
        return MyInfoResponseDto.builder()
                .userId(member.getId())
                .loginId(user.getLoginId())
                .name(member.getName())
                .phone(member.getPhone())
                .birthDate(member.getBirthDate())
                .role(user.getUserRole().name())
                .status(member.getMemberStatus().name())
                .soonId(cell != null ? cell.getCellId() : null)
                .soonName(cell != null ? cell.getCellName() : null)
                .hasAccount(true)
                .memberRoles(member.getRoles())
                .build();
    }
}
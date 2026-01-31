package com.jeja.jejabe.member.domain;

import com.jeja.jejabe.auth.User;
import com.jeja.jejabe.cell.MemberCellHistory;
import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.member.dto.MemberUpdateRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus memberStatus;

    @Enumerated(EnumType.STRING) // DB에는 "MALE", "FEMALE" 문자열로 저장됨
    @Column(length = 10)
    private Gender gender;

    @Column(length = 500) // URL 길이를 고려해 넉넉하게 잡음
    private String memberImageUrl;

    // [핵심 변경] 단일 직분(Position) 대신 역할 목록(Set<Role>) 사용
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "member_roles",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")

    private Set<MemberRole> roles = new HashSet<>();

    // ========================================================================
    // 연관 관계
    // ========================================================================
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private User user;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberCellHistory> cellHistories = new ArrayList<>();

    // ========================================================================
    // 생성자 (Builder 패턴)
    // ========================================================================
    @Builder
    public Member(String name, String phone, LocalDate birthDate, MemberStatus memberStatus, @Singular("role") Set<MemberRole> roles, Gender gender,String memberImageUrl) {
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.memberStatus = memberStatus;
        this.gender = gender;
        this.memberImageUrl = memberImageUrl;
        this.roles = (roles != null) ? roles : new HashSet<>();
    }

    // ========================================================================
    // 비즈니스 로직
    // ========================================================================

    public void update(MemberUpdateRequestDto dto) {
        this.name = dto.getName();
        this.phone = dto.getPhone();
        this.birthDate = dto.getBirthDate();
        this.memberStatus = dto.getMemberStatus();
        this.roles = dto.getRoles();

        // [확인] 이 부분이 있어야 프론트에서 사진 수정 시 반영됨
        if (dto.getMemberImageUrl() != null) {
            this.memberImageUrl = dto.getMemberImageUrl();
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getMember() != this) {
            user.setMember(this);
        }
    }

    public void markAsGraduated() {
        this.memberStatus = MemberStatus.GRADUATED;
    }

    public void markAsNewcomer() {
        this.memberStatus = MemberStatus.NEWCOMER;
    }


    public void updateProfileImage(String imageUrl) {
        this.memberImageUrl = imageUrl;
    }


    // [변경] 유저 승인 시 상태 변경 및 초기 역할 부여
    public void activateMember(MemberStatus status, MemberRole newRole) {
        this.memberStatus = status;
        this.roles.clear();
        this.roles.add(newRole);
    }

    // [추가] 편의 메서드: 역할 하나 추가
    public void addRole(MemberRole role) {
        this.roles.add(role);
    }

    // [추가] 편의 메서드: 역할 하나 제거
    public void removeRole(MemberRole role) {
        this.roles.remove(role);
    }
}
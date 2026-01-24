package com.jeja.jejabe.newcomer.domain;

import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.domain.Gender;
import com.jeja.jejabe.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.sql.ast.spi.LockingClauseStrategy;

import java.time.LocalDate;

@Entity
@Table(name = "newcomer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Newcomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long newcomerId;

    private LocalDate registrationDate; // 등록일
    private String name;
    private Gender gender;
    private LocalDate birthDate;
    private String phone;
    private String address;

    @Column(length = 300)
    private String profileImageUrl; // [통일] 이미지 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_member_id")
    private Member manager; // 담당자

    // [변경] 리포트 대체 필드 3개
    @Column(length = 500)
    private String firstStatus;   // 초기 상태
    @Column(length = 500)
    private String middleStatus;  // 중간 점검
    @Column(length = 500)
    private String recentStatus;  // 최근 근황

    @Lob
    @Column(columnDefinition = "TEXT")
    private String assignmentNote; // 비고/메모

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NewcomerStatus status; // 관리 상태 (MANAGING, GRADUATED 등)

    @Column(nullable = false)
    private boolean isMemberRegistered = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_member_id", unique = true)
    private Member registeredMember;

    @Builder
    public Newcomer(String name, Gender gender, LocalDate birthDate, String phone,
                    Member manager, String profileImageUrl, String address,
                    String firstStatus, String assignmentNote) {
        this.registrationDate = LocalDate.now();
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phone = phone;
        this.manager = manager;
        this.profileImageUrl = profileImageUrl;
        this.address = address;
        this.firstStatus = firstStatus; // 등록 시 초기상태 입력 가능
        this.assignmentNote = assignmentNote;
        this.status = NewcomerStatus.MAIN_WORSHIP;
        this.isMemberRegistered = false;
    }

    // [비즈니스 로직] 전체 정보 수정 (이미지, 상태 텍스트, 담당자 등 포함)
    public void updateInfo(String address, String phone, String assignmentNote,
                           String firstStatus, String middleStatus, String recentStatus,
                           String profileImageUrl, Member newManager, LocalDate birthDate) {
        this.address = address;
        this.phone = phone;
        this.assignmentNote = assignmentNote;
        this.firstStatus = firstStatus;
        this.middleStatus = middleStatus;
        this.recentStatus = recentStatus;
        this.profileImageUrl = profileImageUrl;
        this.birthDate = birthDate;
        if (newManager != null) {
            this.manager = newManager;
        }
    }

    // 관리 상태 변경 (진행중 -> 등반 등)
    public void changeStatus(NewcomerStatus newStatus) {
        this.status = newStatus;
    }

    // 멤버 등반 처리
    public void registerAsMember(Member member) {
        if (this.isMemberRegistered) {
            throw new GeneralException(CommonErrorCode.ALREADY_MEMBER_REGISTERED);
        }
        this.isMemberRegistered = true;
        this.registeredMember = member;
        this.status = NewcomerStatus.YOUTH_WORSHIP; // 등반 시 상태 자동 변경
    }
}
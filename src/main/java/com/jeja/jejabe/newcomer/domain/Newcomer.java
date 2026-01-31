package com.jeja.jejabe.newcomer.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
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
public class Newcomer extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long newcomerId;

    private LocalDate registrationDate;
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthDate;
    private String phone;
    private String address;

    @Column(length = 300)
    private String profileImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_member_id")
    private Member manager;

    @Column(length = 50)
    private String managerName;

    // 리포트용 상태 텍스트
    @Column(length = 500)
    private String firstStatus;
    @Column(length = 500)
    private String middleStatus;
    @Column(length = 500)
    private String recentStatus;

    @Column(length = 500)
    private String cellName;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String assignmentNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NewcomerStatus status;

    // [1] 교회 등록 여부 (행정상 등록) - New!
    @Column(nullable = false)
    private boolean isChurchRegistered = false;

    // [2] 청년부 등록 여부 (등반 여부 / 시스템 멤버 연동)
    @Column(nullable = false)
    private boolean isMemberRegistered = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_member_id", unique = true)
    private Member registeredMember;

    @Builder
    public Newcomer(String name, Gender gender, LocalDate birthDate, String phone,
                    Member manager, String managerName, String profileImageUrl, String address,
                    String firstStatus, String middleStatus, String recentStatus, String assignmentNote,
                    boolean isChurchRegistered) { // 빌더에 추가
        this.registrationDate = LocalDate.now();
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
        this.manager = manager;
        this.managerName = (manager != null) ? manager.getName() : managerName;
        this.firstStatus = firstStatus;
        this.middleStatus = middleStatus;
        this.recentStatus = recentStatus;
        this.assignmentNote = assignmentNote;
        this.status = NewcomerStatus.MAIN_WORSHIP;

        this.isChurchRegistered = isChurchRegistered; // 초기값 설정
        this.isMemberRegistered = false; // 등반은 나중에 함
    }

    // 정보 수정 (교회 등록 여부 포함)
    public void updateInfo(String address, String phone, String assignmentNote,
                           String firstStatus, String middleStatus, String recentStatus,
                           String profileImageUrl, Member newManager, LocalDate birthDate,
                           Boolean isChurchRegistered,Gender gender,String cellName) { // 추가
        if (address != null) this.address = address;
        if (phone != null) this.phone = phone;
        if (assignmentNote != null) this.assignmentNote = assignmentNote;
        if (firstStatus != null) this.firstStatus = firstStatus;
        if (middleStatus != null) this.middleStatus = middleStatus;
        if (recentStatus != null) this.recentStatus = recentStatus;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        if (birthDate != null) this.birthDate = birthDate;
        if (isChurchRegistered != null) this.isChurchRegistered = isChurchRegistered;
        if (gender != null) this.gender = gender;
        if(cellName!=null) this.cellName = cellName;

        if (isChurchRegistered != null) {
            this.isChurchRegistered = isChurchRegistered;
        }

        if (newManager != null) {
            this.manager = newManager;
            this.managerName = newManager.getName();
        }
    }

    public void changeStatus(NewcomerStatus newStatus) {
        this.status = newStatus;
    }

    public void registerAsMember(Member member) {
        if (this.isMemberRegistered) {
            return;
        }
        this.isMemberRegistered = true; // 청년부 등록 완료
        this.registeredMember = member;
        this.status = NewcomerStatus.YOUTH_WORSHIP;
    }

    // Batch용 Setter
    public void setRegistrationDateForBatch(LocalDate date) {
        this.registrationDate = date;
    }

    // Batch용 교회 등록 Setter
    public void setChurchRegisteredForBatch(boolean isRegistered) {
        this.isChurchRegistered = isRegistered;
    }
}
package com.jeja.jejabe.care.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AbsenceCare extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate lastAttendanceDate;
    private int consecutiveAbsenceWeeks;

    @Enumerated(EnumType.STRING)
    private CareStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member manager;

    private LocalDate startDate; // 관리 시작일
    private LocalDate endDate;   // 관리 종료일 (정착 완료일)

    @Column(length = 500)
    private String closingNote;

    @Builder
    public AbsenceCare(Member member, LocalDate lastAttendanceDate, int consecutiveAbsenceWeeks, CareStatus status, LocalDate startDate) {
        this.member = member;
        this.lastAttendanceDate = lastAttendanceDate;
        this.consecutiveAbsenceWeeks = consecutiveAbsenceWeeks;
        this.status = status;
        this.startDate = startDate;
    }

    public void updateStatus(CareStatus status) {
        this.status = status;
    }

    public void updateAbsenceWeeks(int weeks) {
        this.consecutiveAbsenceWeeks = weeks;
    }

    public void updateLastAttendanceDate(LocalDate date) {
        this.lastAttendanceDate = date;
    }

    public void changeManager(Member manager) {
        this.manager = manager;
    }

    public void completeCare(String closingNote) {
        this.status = CareStatus.COMPLETED;
        this.endDate = LocalDate.now();
        this.closingNote = closingNote;
    }

    public void stopCare(String closingNote) {
        this.status = CareStatus.CARE_STOPPED;
        this.endDate = LocalDate.now();
        this.closingNote = closingNote;
    }

//    public void completeCare() {
//        this.status = CareStatus.COMPLETED;
//        this.endDate = LocalDate.now();
//    }
}
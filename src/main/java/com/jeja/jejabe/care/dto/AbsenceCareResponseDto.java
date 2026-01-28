package com.jeja.jejabe.care.dto;

import com.jeja.jejabe.care.domain.AbsenceCare;
import lombok.Data;

import java.time.LocalDate;

// 목록 조회 응답용
@Data
public class AbsenceCareResponseDto {
    private Long careId;
    private Long memberId;
    private String memberImageUrl;
    private String name;
    private String phone;
    private String status;
    private int absenceWeeks;
    private int attendanceWeeks;
    private String managerName;
    private LocalDate startDate;

    public AbsenceCareResponseDto(AbsenceCare care, int attendanceWeeks) {
        this.careId = care.getId();
        this.memberId = care.getMember().getId();
        this.memberImageUrl = care.getMember().getMemberImageUrl();
        this.name = care.getMember().getName();
        this.phone = care.getMember().getPhone();
        this.status = care.getStatus().name();
        this.absenceWeeks = care.getConsecutiveAbsenceWeeks();
        this.attendanceWeeks = attendanceWeeks;
        this.managerName = care.getManager() != null ? care.getManager().getName() : "미정";
        this.startDate = care.getStartDate();
    }
}


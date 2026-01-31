package com.jeja.jejabe.newcomer.dto;

import com.jeja.jejabe.newcomer.domain.Newcomer;
import com.jeja.jejabe.newcomer.domain.NewcomerStatus;
import jakarta.persistence.Column;
import lombok.Getter;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class NewcomerDetailResponseDto {
    private final Long newcomerId;
    private final LocalDate registrationDate;
    private final String name;
    private final String gender;
    private final LocalDate birthDate;
    private final String phone;
    private final String address;
    private final String managerName;
    private final Long managerMemberId;
    private final String assignmentNote;
    private final String cellName;

    // 상태 관련
    private final NewcomerStatus status;       // "MAIN_WORSHIP" (코드용)
    private final String statusDescription;    // "본예배 참석" (화면 표시용)

    // 새로 추가된 필드들
    private final String firstStatus;   // 초기 상태
    private final String middleStatus;  // 중간 점검
    private final String recentStatus;  // 최근 근황
    private final String profileImageUrl;
    private final boolean isChurchRegistered;
    private final boolean isMemberRegistered;

    public NewcomerDetailResponseDto(Newcomer newcomer) {
        this.newcomerId = newcomer.getNewcomerId();
        this.registrationDate = newcomer.getRegistrationDate();
        this.name = newcomer.getName();
        this.gender = newcomer.getGender().getDescription();
        this.birthDate = newcomer.getBirthDate();
        this.phone = newcomer.getPhone();
        this.address = newcomer.getAddress();

        // 담당자 null 체크
        this.managerName = (newcomer.getManager() != null) ? newcomer.getManager().getName() : "미지정";
        this.managerMemberId = (newcomer.getManager() != null) ? newcomer.getManager().getId() : null;

        this.assignmentNote = newcomer.getAssignmentNote();

        // 상태 매핑
        this.status = newcomer.getStatus();
        this.statusDescription = newcomer.getStatus().getDescription(); // Enum에 추가한 한글 설명
        this.cellName = newcomer.getCellName();

        this.isChurchRegistered = newcomer.isChurchRegistered();
        this.isMemberRegistered = newcomer.isMemberRegistered();

        // [수정됨] 누락되었던 필드 매핑 추가
        this.firstStatus = newcomer.getFirstStatus();
        this.middleStatus = newcomer.getMiddleStatus();
        this.recentStatus = newcomer.getRecentStatus();
        this.profileImageUrl = newcomer.getProfileImageUrl();
    }
}

package com.jeja.jejabe.newcomer.dto;

import com.jeja.jejabe.newcomer.domain.Newcomer;
import com.jeja.jejabe.newcomer.domain.NewcomerStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class NewcomerListResponseDto {
    private final Long newcomerId;
    private final String name;
    private final String phone;
    private final String managerName;
    private final LocalDate registrationDate;

    // 상태 관련
    private final NewcomerStatus status;       // "MAIN_WORSHIP" (색상 구분용 코드)
    private final String statusDescription;    // "본예배 참석" (화면 표시용)

    // [추가] 목록 썸네일용 이미지
    private final String profileImageUrl;

    public NewcomerListResponseDto(Newcomer newcomer) {
        this.newcomerId = newcomer.getNewcomerId();
        this.name = newcomer.getName();
        this.phone = newcomer.getPhone();
        this.managerName = (newcomer.getManager() != null) ? newcomer.getManager().getName() : "미지정";
        this.registrationDate = newcomer.getRegistrationDate();

        // 상태 매핑
        this.status = newcomer.getStatus();
        this.statusDescription = newcomer.getStatus().getDescription();

        // 이미지 매핑
        this.profileImageUrl = newcomer.getProfileImageUrl();
    }
}

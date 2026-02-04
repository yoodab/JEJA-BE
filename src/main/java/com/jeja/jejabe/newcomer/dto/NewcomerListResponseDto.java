package com.jeja.jejabe.newcomer.dto;

import com.jeja.jejabe.member.domain.Gender;
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
    private final LocalDate birthDate;
    private final Gender gender;
    private final LocalDate registrationDate;
    private final NewcomerStatus status;
    private final String statusDescription;
    private final String profileImageUrl;
    private final boolean isChurchRegistered;
    private final boolean isMemberRegistered;

    public NewcomerListResponseDto(Newcomer newcomer) {
        this.newcomerId = newcomer.getNewcomerId();
        this.name = newcomer.getName();
        this.phone = newcomer.getPhone();
        this.registrationDate = newcomer.getRegistrationDate();
        this.status = newcomer.getStatus();
        this.statusDescription = newcomer.getStatus().getDescription();
        this.profileImageUrl = newcomer.getProfileImageUrl();
        this.gender = newcomer.getGender();
        this.isChurchRegistered = newcomer.isChurchRegistered();
        this.isMemberRegistered = newcomer.isMemberRegistered();
        this.birthDate = newcomer.getBirthDate();

        // 매니저 이름 스냅샷 우선 사용
        if (newcomer.getManagerName() != null) {
            this.managerName = newcomer.getManagerName();
        } else if (newcomer.getManager() != null) {
            this.managerName = newcomer.getManager().getName();
        } else {
            this.managerName = "미지정";
        }
    }
}
package com.jeja.jejabe.newcomer.dto;

import com.jeja.jejabe.member.domain.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class NewcomerUpdateRequestDto {
    private String phone;
    private String address;
    private Long managerMemberId;
    private String assignmentNote;
    private LocalDate birthDate;
    private String cellName;

    // [추가] 상태 텍스트 3종
    private String firstStatus;
    private String middleStatus;
    private String recentStatus;
    private Boolean isChurchRegistered;
    private Gender gender;
    // [추가] 이미지 변경
    private String profileImageUrl;

}
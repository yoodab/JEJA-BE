package com.jeja.jejabe.newcomer.dto;

import com.jeja.jejabe.member.domain.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class NewcomerBatchRequestDto {
    private String mdName;
    private String registrationDate;
    private String name;
    private Boolean isMemberRegistered;
    private Boolean isChurchRegistered;
    private Gender gender;
    private String birthDate;
    private String phone;
    private String assignedSoon;
    private String address;
    private String firstStatus;
    private String middleStatus;
    private String recentStatus;
    private String assignmentNote;
}

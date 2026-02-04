package com.jeja.jejabe.newcomer.dto;

import com.jeja.jejabe.member.domain.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class NewcomerCreateRequestDto {
    private String name;
    private Gender gender;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private Long managerMemberId;
    private Boolean isChurchRegistered;
    private String firstStatus;
}

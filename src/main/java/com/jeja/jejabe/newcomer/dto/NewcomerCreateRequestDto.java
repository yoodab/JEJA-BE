package com.jeja.jejabe.newcomer.dto;

import com.jeja.jejabe.member.domain.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class NewcomerCreateRequestDto {
    private String name;
    private Gender gender;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private Long managerMemberId;
}

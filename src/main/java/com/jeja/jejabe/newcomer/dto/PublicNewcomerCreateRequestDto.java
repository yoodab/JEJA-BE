package com.jeja.jejabe.newcomer.dto;

import com.jeja.jejabe.member.domain.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PublicNewcomerCreateRequestDto {
    private String name;
    private LocalDate birthDate;
    private Gender gender;
    private String phone;
    private String address;
}

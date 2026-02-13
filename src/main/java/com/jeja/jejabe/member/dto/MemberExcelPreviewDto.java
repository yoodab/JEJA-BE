package com.jeja.jejabe.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeja.jejabe.member.domain.Gender;
import com.jeja.jejabe.member.domain.MemberStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class MemberExcelPreviewDto {
    private String name;
    private LocalDate birthDate;
    private String phone;
    private Gender gender;
    private MemberStatus memberStatus;

    @JsonProperty("isDuplicate")
    private boolean isDuplicate;
}

package com.jeja.jejabe.newcomer.dto;

import com.jeja.jejabe.newcomer.domain.MdAssignment;
import lombok.Getter;

@Getter
public class MdAssignmentResponseDto {
    private final Long id;
    private final Long memberId;
    private final String name;
    private final String gender;
    private final String phone;
    private final String charge;
    private final String ageGroup;

    public MdAssignmentResponseDto(MdAssignment md) {
        this.id = md.getId();
        this.memberId = md.getMember().getId();
        this.name = md.getMember().getName();
        this.gender = md.getMember().getGender().getDescription();
        this.phone = md.getMember().getPhone();
        this.charge = md.getCharge();
        this.ageGroup = md.getAgeGroup();
    }
}
package com.jeja.jejabe.newcomer.dto;

import com.jeja.jejabe.newcomer.domain.NewcomerStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NewcomerCreateRequestDto {
    private String name;
    private String gender;
    private String birthDate;
    private String phone;
    private String address;
    private Long managerMemberId;
}

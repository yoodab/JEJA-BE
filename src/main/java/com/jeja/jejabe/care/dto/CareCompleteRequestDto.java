package com.jeja.jejabe.care.dto;

import lombok.Data;

@Data
public class CareCompleteRequestDto {
    private String type;        // "COMPLETED" (정착) 또는 "STOPPED" (중단)
    private String closingNote; // 종료 사유
}

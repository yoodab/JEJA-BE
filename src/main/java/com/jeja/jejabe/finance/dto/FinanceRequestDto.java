package com.jeja.jejabe.finance.dto;

import com.jeja.jejabe.finance.domain.FinanceType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FinanceRequestDto {
    private LocalDate date;
    private FinanceType type;
    private String categoryName; // 입력 시 이름으로 받음 (또는 ID로 받아도 됨)
    private String detail;
    private Long amount;
    private String receiptUrl;
    private Long scheduleId;
}

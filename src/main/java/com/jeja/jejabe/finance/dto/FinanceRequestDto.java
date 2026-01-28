package com.jeja.jejabe.finance.dto;

import com.jeja.jejabe.finance.domain.FinanceType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FinanceRequestDto {
    private LocalDate date;
    private FinanceType transactionType;
    private String category;
    private String detail;
    private Long amount;
    private String description;
    private List<String> receiptImages;
    private Long scheduleId;
}

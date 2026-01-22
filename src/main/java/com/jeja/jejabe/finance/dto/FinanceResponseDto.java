package com.jeja.jejabe.finance.dto;

import com.jeja.jejabe.finance.domain.Finance;
import com.jeja.jejabe.finance.domain.FinanceType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FinanceResponseDto {
    private Long id;
    private LocalDate date;
    private String type;
    private String category;
    private String detail;
    private Long income;
    private Long expense;
    private Long balance;
    private String receiptUrl;
    private String relatedEvent;

    public FinanceResponseDto(Finance finance, Long currentBalance) {
        this.id = finance.getId();
        this.date = finance.getTransactionDate();
        this.type = finance.getType().getDescription();
        this.category = finance.getCategory().getName(); // 엔티티에서 이름 추출
        this.detail = finance.getDetail();

        if (finance.getType() == FinanceType.INCOME) {
            this.income = finance.getAmount();
            this.expense = 0L;
        } else {
            this.income = 0L;
            this.expense = finance.getAmount();
        }

        this.balance = currentBalance;
        this.receiptUrl = finance.getReceiptUrl();
        if (finance.getSchedule() != null) {
            this.relatedEvent = finance.getSchedule().getTitle();
        }
    }
}
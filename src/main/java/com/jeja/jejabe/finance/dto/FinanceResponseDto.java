package com.jeja.jejabe.finance.dto;

import com.jeja.jejabe.finance.domain.Finance;
import com.jeja.jejabe.finance.domain.FinanceType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FinanceResponseDto {
    private Long id;
    private LocalDate date;
    private FinanceType transactionType;
    private String category;
    private String detail;
    private Long amount;
    private Long balance;
    private List<String> receiptImages;

    public FinanceResponseDto(Finance finance, Long currentBalance) {
        this.id = finance.getId();
        this.date = finance.getTransactionDate();
        this.transactionType = finance.getType();
        this.category = finance.getCategory().getName();
        this.detail = finance.getDetail();
        this.amount = finance.getAmount();
        this.balance = currentBalance;
        this.receiptImages = finance.getReceiptImages();
    }
}
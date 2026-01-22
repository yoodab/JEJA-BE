package com.jeja.jejabe.finance.domain;

public enum FinanceType {
    INCOME("수입"),
    EXPENSE("지출");

    private final String description;

    FinanceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package com.jeja.jejabe.finance.dto;

import lombok.Data;

import java.util.List;

@Data
public class YearlyReportDto {
    private int year;
    private Summary summary;
    private List<MonthlyStat> monthlyStats;
    private List<CategoryStat> incomeCategories;
    private List<CategoryStat> expenseCategories;
    private List<EventStat> topEvents;

    @Data
    public static class Summary {
        private Long totalIncome;
        private Long totalExpense;
        private Long netBalance;
    }

    @Data
    public static class MonthlyStat {
        private int month;
        private Long income;
        private Long expense;
    }

    @Data
    public static class CategoryStat {
        private String category;
        private Long amount;
        private Double percentage;
    }

    @Data
    public static class EventStat {
        private String eventName;
        private Long totalExpense;
    }
}

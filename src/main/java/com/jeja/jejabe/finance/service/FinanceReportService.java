package com.jeja.jejabe.finance.service;

import com.jeja.jejabe.finance.repository.FinanceRepository;
import com.jeja.jejabe.finance.domain.Finance;
import com.jeja.jejabe.finance.domain.FinanceType;
import com.jeja.jejabe.finance.dto.YearlyReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceReportService {

    private final FinanceRepository financeRepository;

    public YearlyReportDto getYearlyReport(int year) {
        YearlyReportDto report = new YearlyReportDto();
        report.setYear(year);

        List<Finance> records = financeRepository.findAllByTransactionDateBetweenOrderByTransactionDateAsc(
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));

        // 1. 요약
        long totalIncome = records.stream().filter(f -> f.getType() == FinanceType.INCOME).mapToLong(Finance::getAmount).sum();
        long totalExpense = records.stream().filter(f -> f.getType() == FinanceType.EXPENSE).mapToLong(Finance::getAmount).sum();

        YearlyReportDto.Summary summary = new YearlyReportDto.Summary();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setNetBalance(totalIncome - totalExpense);
        report.setSummary(summary);

        // 2. 월별 통계
        List<YearlyReportDto.MonthlyStat> monthlyStats = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            int m = i;
            long inc = records.stream().filter(f -> f.getTransactionDate().getMonthValue() == m && f.getType() == FinanceType.INCOME).mapToLong(Finance::getAmount).sum();
            long exp = records.stream().filter(f -> f.getTransactionDate().getMonthValue() == m && f.getType() == FinanceType.EXPENSE).mapToLong(Finance::getAmount).sum();

            YearlyReportDto.MonthlyStat stat = new YearlyReportDto.MonthlyStat();
            stat.setMonth(m);
            stat.setIncome(inc);
            stat.setExpense(exp);
            monthlyStats.add(stat);
        }
        report.setMonthlyStats(monthlyStats);

        // 3. 항목별 비중
        report.setIncomeCategories(calculateCategoryStats(records, FinanceType.INCOME, totalIncome));
        report.setExpenseCategories(calculateCategoryStats(records, FinanceType.EXPENSE, totalExpense));

        // 4. 행사별 지출 TOP 5
        Map<String, Long> eventMap = records.stream()
                .filter(f -> f.getType() == FinanceType.EXPENSE && f.getSchedule() != null)
                .collect(Collectors.groupingBy(f -> f.getSchedule().getTitle(), Collectors.summingLong(Finance::getAmount)));

        report.setTopEvents(eventMap.entrySet().stream()
                .map(e -> {
                    YearlyReportDto.EventStat stat = new YearlyReportDto.EventStat();
                    stat.setEventName(e.getKey());
                    stat.setTotalExpense(e.getValue());
                    return stat;
                })
                .sorted((a, b) -> Long.compare(b.getTotalExpense(), a.getTotalExpense()))
                .limit(5).collect(Collectors.toList()));

        return report;
    }

    private List<YearlyReportDto.CategoryStat> calculateCategoryStats(List<Finance> records, FinanceType type, long total) {
        return records.stream()
                .filter(f -> f.getType() == type)
                .collect(Collectors.groupingBy(f -> f.getCategory().getName(), Collectors.summingLong(Finance::getAmount)))
                .entrySet().stream()
                .map(e -> {
                    YearlyReportDto.CategoryStat stat = new YearlyReportDto.CategoryStat();
                    stat.setCategory(e.getKey());
                    stat.setAmount(e.getValue());
                    stat.setPercentage(total == 0 ? 0.0 : Math.round((e.getValue() * 100.0 / total) * 10) / 10.0);
                    return stat;
                })
                .sorted((a, b) -> Long.compare(b.getAmount(), a.getAmount()))
                .collect(Collectors.toList());
    }
}

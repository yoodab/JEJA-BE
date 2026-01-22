package com.jeja.jejabe.finance.repository;

import com.jeja.jejabe.finance.domain.Finance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FinanceRepository extends JpaRepository<Finance, Long> {
    // 날짜 범위 조회 (잔액 계산을 위해 날짜순 정렬 필수)
    List<Finance> findAllByTransactionDateBetweenOrderByTransactionDateAsc(LocalDate startDate, LocalDate endDate);
}

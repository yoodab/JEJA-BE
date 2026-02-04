package com.jeja.jejabe.finance.repository;

import com.jeja.jejabe.finance.domain.Finance;
import com.jeja.jejabe.finance.domain.FinanceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FinanceRepository extends JpaRepository<Finance, Long> {
    // 날짜 범위 조회 (잔액 계산을 위해 날짜순 정렬 필수)
    List<Finance> findAllByTransactionDateBetweenOrderByTransactionDateAsc(LocalDate startDate, LocalDate endDate);
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Finance f SET f.category = :newCategory WHERE f.category = :oldCategory")
    void bulkUpdateCategory(@Param("oldCategory") FinanceCategory oldCategory,
                            @Param("newCategory") FinanceCategory newCategory);
}

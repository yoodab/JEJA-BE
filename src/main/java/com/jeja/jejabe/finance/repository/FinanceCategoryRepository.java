package com.jeja.jejabe.finance.repository;

import com.jeja.jejabe.finance.domain.FinanceCategory;
import com.jeja.jejabe.finance.domain.FinanceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinanceCategoryRepository extends JpaRepository<FinanceCategory, Long> {
    // 타입별 목록 조회 (화면 드롭다운용)
    List<FinanceCategory> findAllByType(FinanceType type);

    // 이름과 타입으로 찾기 (엑셀 업로드 시 중복 방지용)
    Optional<FinanceCategory> findByNameAndType(String name, FinanceType type);
}

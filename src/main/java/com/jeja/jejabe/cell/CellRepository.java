package com.jeja.jejabe.cell;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CellRepository extends JpaRepository<Cell, Long> {
    // 특정 연도의 모든 셀을 찾는 기능
    List<Cell> findAllByYear(Integer year);
}

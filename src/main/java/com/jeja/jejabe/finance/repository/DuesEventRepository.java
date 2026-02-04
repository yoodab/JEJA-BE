package com.jeja.jejabe.finance.repository;

import com.jeja.jejabe.finance.domain.DuesEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DuesEventRepository extends JpaRepository<DuesEvent, Long> {
    List<DuesEvent> findAllByOrderByDateDesc();
}

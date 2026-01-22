package com.jeja.jejabe.homepage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SlideRepository extends JpaRepository<Slide, Long> {
    // 순서대로 조회
    List<Slide> findAllByOrderByOrderIndexAsc();

    // 마지막 순서 값 가져오기 (새 슬라이드 추가 시 사용)
    @Query("SELECT MAX(s.orderIndex) FROM Slide s")
    Integer findMaxOrderIndex();
}

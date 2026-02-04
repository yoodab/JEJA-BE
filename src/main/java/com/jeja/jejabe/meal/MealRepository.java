package com.jeja.jejabe.meal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface MealRepository extends JpaRepository<MealHistory, Long> {
    // 날짜 최신순 정렬해서 가져오기
    List<MealHistory> findAllByOrderByDateDescIdDesc();
}

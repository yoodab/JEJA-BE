package com.jeja.jejabe.meal;

import java.time.LocalDate;
import java.util.List;

public record MealResponse(
        int currentStock,          // 현재 남은 재고
        List<HistoryDto> history   // 내역 리스트
) {
    public record HistoryDto(
            Long id,
            LocalDate date,
            MealCategory category,
            String targetName,
            String note,
            int amount
    ) {}
}

// 2. 재고 추가 요청용


// 3. 사용 요청용


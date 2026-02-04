package com.jeja.jejabe.meal;

import java.time.LocalDate;

public record MealUpdateRequest(
        LocalDate date,    // 날짜 수정 가능
        String targetName, // 이름 수정
        String note,       // 비고 수정
        int amount         // 개수 수정 (사용일 경우 양수로 보내면 서버가 음수로 변환)
) {}

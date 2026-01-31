package com.jeja.jejabe.meal;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meal_history")
public class MealHistory extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;       // 날짜

    @Enumerated(EnumType.STRING)
    private MealCategory category; // STOCK(입고), USE(사용)

    private String targetName;    // 대상자 이름 (없으면 "관리자")
    private String note;          // 비고/장소
    private int amount;           // 수량 (+10, -1)

    @Builder
    public MealHistory(MealCategory category, String targetName, String note, int amount) {
        this.date = LocalDate.now();
        this.category = category;
        this.targetName = targetName;
        this.note = note;
        this.amount = amount;
    }

    // ★ 핵심: 수정 메서드 (비즈니스 로직 포함)
    public void update(LocalDate date, String targetName, String note, int requestAmount) {
        // 1. 단순 필드 수정
        if (date != null) this.date = date;
        if (targetName != null) this.targetName = targetName;
        if (note != null) this.note = note;

        // 2. 수량 계산 로직 (엔티티가 스스로 판단)
        // 요청된 수량이 0이 아닐 때만 업데이트 수행
        if (requestAmount != 0) {
            int absAmount = Math.abs(requestAmount); // 일단 절대값으로 만듦

            if (this.category == MealCategory.USE) {
                this.amount = -absAmount; // 사용(USE)이면 음수로 저장
            } else {
                this.amount = absAmount;  // 입고(STOCK)면 양수로 저장
            }
        }
    }
}
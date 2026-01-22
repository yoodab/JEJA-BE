package com.jeja.jejabe.care.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CareConfig {
    @Id
    private Long id = 1L; // 단일 설정값

    private int attentionWeeksThreshold = 2;    // 관심필요 기준 (2주)
    private int longTermWeeksThreshold = 4;     // 장기결석 기준 (4주)
    private int resettlementWeeksThreshold = 2; // 재정착 완료 기준 (2주)
}

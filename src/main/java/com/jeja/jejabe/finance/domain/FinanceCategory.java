package com.jeja.jejabe.finance.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinanceCategory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 항목명 (예: 주일헌금, 식비)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinanceType type; // 수입용인지 지출용인지 구분

    @Builder
    public FinanceCategory(String name, FinanceType type) {
        this.name = name;
        this.type = type;
    }
}

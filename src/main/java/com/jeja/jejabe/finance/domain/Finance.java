package com.jeja.jejabe.finance.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.schedule.domain.Schedule;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Finance extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate transactionDate; // 거래 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinanceType type; // 수입/지출

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FinanceCategory category;

    private String detail; // 세부내용 (김철수, 이마트 장보기...)

    @Column(nullable = false)
    private Long amount; // 금액

    private String receiptUrl; // 영수증 이미지 URL

    // 관련 일정 (선택사항: 수련회 등 행사와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @Builder
    public Finance(LocalDate transactionDate, FinanceType type, FinanceCategory category, String detail, Long amount, String receiptUrl, Schedule schedule) {
        this.transactionDate = transactionDate;
        this.type = type;
        this.category = category;
        this.detail = detail;
        this.amount = amount;
        this.receiptUrl = receiptUrl;
        this.schedule = schedule;
    }

    public void update(LocalDate transactionDate, FinanceType type, FinanceCategory category, String detail, Long amount, String receiptUrl, Schedule schedule) {
        this.transactionDate = transactionDate;
        this.type = type;
        this.category = category;
        this.detail = detail;
        this.amount = amount;
        this.receiptUrl = receiptUrl;
        this.schedule = schedule;
    }
}

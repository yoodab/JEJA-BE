package com.jeja.jejabe.finance.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import com.jeja.jejabe.schedule.domain.Schedule;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Finance extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinanceType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FinanceCategory category;

    private String detail;

    @Column(nullable = false)
    private Long amount;

    // 다중 영수증 이미지
    @ElementCollection
    @CollectionTable(name = "finance_receipts", joinColumns = @JoinColumn(name = "finance_id"))
    @Column(name = "image_url", length = 5000)
    private List<String> receiptImages = new ArrayList<>();



    @Builder
    public Finance(LocalDate transactionDate, FinanceType type, FinanceCategory category, String detail, Long amount, List<String> receiptImages) {
        this.transactionDate = transactionDate;
        this.type = type;
        this.category = category;
        this.detail = detail;
        this.amount = amount;
        this.receiptImages = receiptImages != null ? receiptImages : new ArrayList<>();
    }

    public void update(LocalDate transactionDate, FinanceType type, FinanceCategory category, String detail, Long amount, List<String> receiptImages) {
        this.transactionDate = transactionDate;
        this.type = type;
        this.category = category;
        this.detail = detail;
        this.amount = amount;
        this.receiptImages = receiptImages;
    }
}
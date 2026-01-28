package com.jeja.jejabe.finance.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DuesRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private DuesEvent event;

    private String memberName;

    private Long paidAmount;
    private Long expectedAmount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private LocalDate paymentDate;
    private String note;

    @Builder
    public DuesRecord(DuesEvent event, String memberName, Long paidAmount, Long expectedAmount, PaymentMethod paymentMethod, LocalDate paymentDate, String note) {
        this.event = event;
        this.memberName = memberName;
        this.paidAmount = paidAmount;
        this.expectedAmount = expectedAmount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.note = note;
    }

    public void update(Long paidAmount, Long expectedAmount, PaymentMethod paymentMethod, LocalDate paymentDate, String note) {
        this.paidAmount = paidAmount;
        this.expectedAmount = expectedAmount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.note = note;
    }
}

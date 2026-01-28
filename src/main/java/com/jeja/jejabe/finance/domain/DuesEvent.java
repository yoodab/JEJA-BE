package com.jeja.jejabe.finance.domain;

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
public class DuesEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Long targetAmount;
    private LocalDate date;

    private LocalDate targetDate;

    private Long scheduleId;

    @ElementCollection
    @CollectionTable(name = "dues_price_options", joinColumns = @JoinColumn(name = "event_id"))
    private List<DuesPriceOption> priceOptions = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DuesRecord> records = new ArrayList<>();

    @Builder
    public DuesEvent(String name, Long targetAmount, LocalDate date, LocalDate targetDate, Long scheduleId, List<DuesPriceOption> priceOptions) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.date = date;
        this.targetDate = targetDate;
        this.scheduleId = scheduleId;
        this.priceOptions = priceOptions;
    }

    public void update(String name, Long targetAmount, LocalDate date,LocalDate targetDate, Long scheduleId, List<DuesPriceOption> priceOptions) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.date = date;
        this.targetDate = targetDate;
        this.scheduleId = scheduleId;
        this.priceOptions = priceOptions;
    }
}

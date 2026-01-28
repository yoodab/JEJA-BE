package com.jeja.jejabe.finance.dto;

import com.jeja.jejabe.finance.domain.DuesPriceOption;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DuesEventDto {
    private Long id;
    private String name;
    private Long targetAmount;
    private LocalDate date;
    private LocalDate targetDate;
    private Long scheduleId;
    private List<DuesPriceOption> priceOptions;
}

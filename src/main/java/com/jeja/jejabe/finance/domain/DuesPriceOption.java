package com.jeja.jejabe.finance.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DuesPriceOption {
    private String optionId;
    private String name;
    private Long amount;
}

package com.jeja.jejabe.finance.dto;

import com.jeja.jejabe.finance.domain.FinanceType;
import lombok.Data;

@Data
public class CategoryDto {
    private Long id;
    private String name;
    private FinanceType type;
}

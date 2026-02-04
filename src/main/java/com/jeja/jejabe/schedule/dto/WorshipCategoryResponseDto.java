package com.jeja.jejabe.schedule.dto;

import com.jeja.jejabe.schedule.domain.WorshipCategory;

public record WorshipCategoryResponseDto(String code, String name) {
    public static WorshipCategoryResponseDto from(WorshipCategory category) {
        return new WorshipCategoryResponseDto(category.name(), category.getDescription());
    }
}

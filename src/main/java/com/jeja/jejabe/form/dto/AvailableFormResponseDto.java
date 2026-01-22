package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.FormCategory;
import com.jeja.jejabe.form.domain.FormTemplate;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AvailableFormResponseDto {
    private Long templateId;
    private String title;
    private String description;
    private FormCategory category;
    private boolean isSubmitted;
    private LocalDate lastSubmitDate;

    public AvailableFormResponseDto(FormTemplate t, boolean isSubmitted, LocalDate lastSubmitDate) {
        this.templateId = t.getId();
        this.title = t.getTitle();
        this.description = t.getDescription();
        this.category = t.getCategory();
        this.isSubmitted = isSubmitted;
        this.lastSubmitDate = lastSubmitDate;
    }
}

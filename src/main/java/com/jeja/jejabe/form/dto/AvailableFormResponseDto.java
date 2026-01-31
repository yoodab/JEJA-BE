package com.jeja.jejabe.form.dto;

import com.jeja.jejabe.form.domain.FormCategory;
import com.jeja.jejabe.form.domain.FormTemplate;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AvailableFormResponseDto {
    private Long templateId;
    private String title;
    private String description;
    private FormCategory category;

    private boolean isSubmitted;      // 제출 완료 여부 (일반 폼용)
    private LocalDate lastSubmitDate;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String statusMessage;     // "D-Day", "3건 미제출" 등

    // [핵심] 순 보고서용 선택 가능한 날짜 목록 (드롭다운용)
    private List<LocalDate> selectableDates;

    public AvailableFormResponseDto(FormTemplate t, boolean isSubmitted, LocalDate lastSubmitDate) {
        this.templateId = t.getId();
        this.title = t.getTitle();
        this.description = t.getDescription();
        this.category = t.getCategory();
        this.isSubmitted = isSubmitted;
        this.lastSubmitDate = lastSubmitDate;

        this.startDate = t.getStartDate();
        this.endDate = t.getEndDate();

        if (t.getCategory() != FormCategory.CELL_REPORT) {
            if (isSubmitted) this.statusMessage = "제출 완료";
            else this.statusMessage = "접수 중";
        }
    }
}

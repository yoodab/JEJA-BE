package com.jeja.jejabe.care.dto;

import com.jeja.jejabe.care.domain.AbsenceCare;
import lombok.Data;

@Data
public class AbsenceCareHistoryDto {
    private Long id;
    private String status;
    private String startDate;
    private String endDate;
    private String managerName;
    private String closingNote;

    public AbsenceCareHistoryDto(AbsenceCare care) {
        this.id = care.getId();
        this.status = care.getStatus().name();
        this.startDate = care.getStartDate().toString();
        this.endDate = care.getEndDate() != null ? care.getEndDate().toString() : "-";
        this.managerName = care.getManager() != null ? care.getManager().getName() : "미정";
        this.closingNote = care.getClosingNote();
    }
}

package com.jeja.jejabe.care.dto;

import com.jeja.jejabe.care.domain.CareLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CareLogDto {
    private Long logId;
    private String content;
    private String managerName;
    private LocalDateTime careDate;
    private String careMethod;

    public CareLogDto(CareLog log) {
        this.logId = log.getId();
        this.content = log.getContent();
        this.managerName = log.getManager().getName();
        this.careDate = log.getCareDate();
        this.careMethod = log.getCareMethod();
    }
}

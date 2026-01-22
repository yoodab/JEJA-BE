package com.jeja.jejabe.care.dto;

import lombok.Data;

import java.util.List;

@Data
public class AbsenceCareDetailDto {
    private AbsenceCareResponseDto currentInfo;
    private List<AbsenceCareHistoryDto> history;
    private List<CareLogDto> logs;

    public AbsenceCareDetailDto(AbsenceCareResponseDto currentInfo, List<AbsenceCareHistoryDto> history, List<CareLogDto> logs) {
        this.currentInfo = currentInfo;
        this.history = history;
        this.logs = logs;
    }
}

package com.jeja.jejabe.cell.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CellMemberBatchUpdateRequestDto {

    private List<CellUpdateInfo> cellUpdates;

    @Getter
    @NoArgsConstructor
    public static class CellUpdateInfo {
        private Long cellId;
        private Long leaderId;
        private List<Long> memberIds;
    }
}

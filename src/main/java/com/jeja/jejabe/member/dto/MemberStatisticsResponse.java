package com.jeja.jejabe.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class MemberStatisticsResponse {
    private long totalCount;    // 전체 인원 (재적 + 새신자 + 휴면 합계)
    private long activeCount;   // 재적 (ACTIVE)
    private long inactiveCount; // 휴면 (LONG_TERM_ABSENT + MOVED)
    private long newcomerCount; // 새신자 (NEWCOMER)
}

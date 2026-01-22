package com.jeja.jejabe.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardStatsDto {
    private long weeklyAttendanceCount;
    private long monthlyNewcomerCount;
    private long longTermAbsenteeCount;
    private long pendingUserCount;
}

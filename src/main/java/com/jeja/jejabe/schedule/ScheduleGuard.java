package com.jeja.jejabe.schedule;

import com.jeja.jejabe.auth.UserDetailsImpl;
import com.jeja.jejabe.auth.UserRole;
import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.schedule.domain.Schedule;
import com.jeja.jejabe.schedule.domain.SharingScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("scheduleGuard")
@RequiredArgsConstructor
public class ScheduleGuard {

    private final ScheduleRepository scheduleRepository;

    // 일정 수정/삭제 권한 (관리자, 교역자, 임원만 가능)
    // 작성자 개념이 Schedule 엔티티에 없다면 관리자 그룹만 가능하게 설정
    public boolean canManageSchedule(UserDetailsImpl userDetails) {
        if (userDetails == null) return false;
        return isAdminGroup(userDetails);
    }

    // 일정 상세 조회 권한 (비공개 일정 필터링)
    @Transactional(readOnly = true)
    public boolean canReadSchedule(Long scheduleId, UserDetailsImpl userDetails) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new GeneralException(CommonErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getSharingScope() == SharingScope.PUBLIC) return true;

        // 로그인 필요
        if (userDetails == null) return false;
        if (schedule.getSharingScope() == SharingScope.LOGGED_IN_USERS) return true;

        // 비공개(PRIVATE)는 관리자 그룹만
        return isAdminGroup(userDetails);
    }

    private boolean isAdminGroup(UserDetailsImpl userDetails) {
        return userDetails != null && userDetails.getUser().isPrivileged();
    }
}

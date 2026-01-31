package com.jeja.jejabe.form.domain;

public enum AttendanceSyncType {
    NONE,               // 연동 없음 (일반 질문)
    PRE_REGISTRATION,   // 사전 참여 신청 (상태: REGISTERED) - 수련회/행사 신청 등
    POST_CONFIRMATION   // 사후 출석 확정 (상태: PRESENT) - 순 보고서 출석 체크 등
}

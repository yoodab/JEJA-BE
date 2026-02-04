package com.jeja.jejabe.attendance;

public enum AttendanceSource {
    GPS,            // 본인 GPS 출석
    LEADER,         // 순장 보고서 체크
    ADMIN,          // 관리자 수기 입력
    GPS_AND_LEADER,  // GPS + 순장 확인 (교차 검증됨)
    GUEST_PAGE      // 게스트 페이지(이름 입력)
}

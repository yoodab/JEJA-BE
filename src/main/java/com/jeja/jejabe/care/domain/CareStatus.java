package com.jeja.jejabe.care.domain;

public enum CareStatus {
    NEEDS_ATTENTION,   // 관심 필요 (2주 결석)
    LONG_TERM_ABSENCE, // 장기 결석 (4주 결석)
    RESETTLING,        // 재정착 (복귀 후 적응 중)
    COMPLETED,         // 정착 완료 (성공 - 배치 자동 처리 or 수동 정착)
    CARE_STOPPED       // 케어 중단 (실패/종료 - 이사, 타교회, 거부 등)       // 정착 완료 (이력 보관용)
}

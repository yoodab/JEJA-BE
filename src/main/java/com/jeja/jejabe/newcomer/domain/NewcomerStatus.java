package com.jeja.jejabe.newcomer.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NewcomerStatus {

    // [변경] MANAGING 분리
    MAIN_WORSHIP("본예배 참석"),   // 본예배만 드리고 바로 가는 상태
    YOUTH_WORSHIP("청년부 예배"),  // 청년부 예배/모임까지 참석하는 상태

    // 기존 상태 유지
    HOLD("보류"),                 // 장기 결석, 연락 두절 등 잠시 멈춤
    SETTLED("정착 완료"),          // (등반 직전) 완전히 적응함
    STOPPED("관리 중단");          // 타교회 이동, 이사 등으로 종료

    private final String description;
}

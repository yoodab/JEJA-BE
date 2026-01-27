package com.jeja.jejabe.schedule.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorshipCategory {
    SUNDAY_SERVICE_1("주일예배 1부"),
    SUNDAY_SERVICE_2("주일예배 2부"),
    SUNDAY_SERVICE_3("주일예배 3부"),
    WEDNESDAY_SERVICE_1("수요예배 1부"),
    WEDNESDAY_SERVICE_2("수요예배 2부"),
    FRIDAY_PRAYER("금요기도회"),
    DAWN_PRAYER("새벽기도회"),
    YOUTH_SERVICE("청년부 예배"),
    ETC("기타");

    private final String description;
}

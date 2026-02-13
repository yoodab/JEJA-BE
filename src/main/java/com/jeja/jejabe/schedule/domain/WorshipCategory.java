package com.jeja.jejabe.schedule.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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

    @JsonValue
    public String getName() {
        return name();
    }

    @JsonCreator
    public static WorshipCategory fromString(String value) {
        if (value == null) return null;
        try {
            return WorshipCategory.valueOf(value);
        } catch (IllegalArgumentException e) {
            // 숫자로 들어오는 경우 처리 (레거시 대응)
            try {
                int index = Integer.parseInt(value);
                if (index >= 0 && index < values().length) {
                    return values()[index];
                }
            } catch (NumberFormatException nfe) {
                // Ignore
            }
            return null;
        }
    }
}

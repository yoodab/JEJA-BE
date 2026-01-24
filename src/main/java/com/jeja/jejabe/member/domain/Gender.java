package com.jeja.jejabe.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Gender {
    MALE("남자"),
    FEMALE("여자"),
    NONE("미입력");

    private final String description;
}

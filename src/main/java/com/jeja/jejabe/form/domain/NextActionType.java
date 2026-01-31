package com.jeja.jejabe.form.domain;

public enum NextActionType {
    CONTINUE,      // 다음 섹션으로 이동 (기본값)
    GO_TO_SECTION, // 특정 섹션으로 점프
    SUBMIT         // 설문 종료 및 제출
}

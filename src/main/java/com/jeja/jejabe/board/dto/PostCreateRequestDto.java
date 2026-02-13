package com.jeja.jejabe.board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateRequestDto {
    private String title;
    private String content;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

    @JsonProperty("isNotice")
    private boolean isNotice;

    private String attachmentUrl; // 선행 업로드된 파일 경로
    private String attachmentName; // 원본 파일명

    @JsonProperty("isPrivate")
    public boolean isPrivate() {
        return isPrivate;
    }

    @JsonProperty("isPrivate")
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @JsonProperty("isNotice")
    public boolean isNotice() {
        return isNotice;
    }

    @JsonProperty("isNotice")
    public void setNotice(boolean isNotice) {
        this.isNotice = isNotice;
    }
}
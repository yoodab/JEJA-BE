package com.jeja.jejabe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateRequestDto {
    private String title;
    private String content;
    private boolean isPrivate;
    private boolean isNotice;
    private String attachmentUrl;  // 선행 업로드된 파일 경로
    private String attachmentName; // 원본 파일명
}
package com.jeja.jejabe.homepage.dto;

import lombok.Data;

import java.util.List;

@Data
public class SlideRequestDto {
    private String type; // "text" or "image"

    // 이미지용 (이미 업로드된 URL)
    private String url;
    private String linkUrl;
    private String title;
    private String subtitle;

    // 텍스트용
    private String backgroundColor;
    private List<TextElementDto> textElements;
}

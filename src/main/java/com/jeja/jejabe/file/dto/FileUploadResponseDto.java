package com.jeja.jejabe.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileUploadResponseDto {
    private String url;           // 저장된 경로 (/files/uuid_파일명.jpg)
    private String originalName;  // 원본 파일명 (image.jpg)
}

package com.jeja.jejabe.homepage.dto;

import lombok.Data;

import java.util.List;

@Data
public class SlideReorderRequestDto {
    private List<Long> slideIds; // 순서대로 정렬된 ID 리스트
}

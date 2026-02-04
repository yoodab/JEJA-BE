package com.jeja.jejabe.rollingpaper.dto;

import com.jeja.jejabe.rollingpaper.domain.RollingPaper;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RollingPaperCreateRequestDto {
    private String title;
    private String theme;
    private String backgroundConfig;

    public RollingPaper toEntity() {
        return RollingPaper.builder()
                .title(title)
                .theme(theme)
                .backgroundConfig(backgroundConfig)
                .build();
    }
}

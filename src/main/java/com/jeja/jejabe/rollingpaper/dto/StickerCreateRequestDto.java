package com.jeja.jejabe.rollingpaper.dto;

import com.jeja.jejabe.rollingpaper.domain.RollingPaper;
import com.jeja.jejabe.rollingpaper.domain.RollingPaperSticker;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StickerCreateRequestDto {
    private String stickerUrl;
    private Double posX;
    private Double posY;
    private Double scale;
    private Double rotation;
    private Integer zIndex;

    public RollingPaperSticker toEntity(RollingPaper rollingPaper) {
        return RollingPaperSticker.builder()
                .rollingPaper(rollingPaper)
                .stickerUrl(stickerUrl)
                .posX(posX)
                .posY(posY)
                .scale(scale)
                .rotation(rotation)
                .zIndex(zIndex)
                .build();
    }
}

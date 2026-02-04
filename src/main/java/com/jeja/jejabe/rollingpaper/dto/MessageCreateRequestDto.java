package com.jeja.jejabe.rollingpaper.dto;

import com.jeja.jejabe.rollingpaper.domain.RollingPaper;
import com.jeja.jejabe.rollingpaper.domain.RollingPaperMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageCreateRequestDto {
    private String content;
    private String authorName;
    private boolean isAnonymous;
    private String backgroundColor;
    private String backgroundImage;
    private String fontFamily;
    private String fontColor;
    private String textAlign;
    private Double posX;
    private Double posY;
    private Double rotation;
    private Integer zIndex;

    public RollingPaperMessage toEntity(RollingPaper rollingPaper) {
        return RollingPaperMessage.builder()
                .rollingPaper(rollingPaper)
                .content(content)
                .authorName(authorName)
                .isAnonymous(isAnonymous)
                .backgroundColor(backgroundColor)
                .backgroundImage(backgroundImage)
                .fontFamily(fontFamily)
                .fontColor(fontColor)
                .textAlign(textAlign)
                .posX(posX)
                .posY(posY)
                .rotation(rotation)
                .zIndex(zIndex)
                .build();
    }
}

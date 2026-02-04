package com.jeja.jejabe.rollingpaper.dto;

import com.jeja.jejabe.rollingpaper.domain.RollingPaper;
import com.jeja.jejabe.rollingpaper.domain.RollingPaperMessage;
import com.jeja.jejabe.rollingpaper.domain.RollingPaperSticker;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RollingPaperResponseDto {
    private Long id;
    private String title;
    private String theme;
    private String backgroundConfig;
    private List<MessageDto> messages;
    private List<StickerDto> stickers;

    public RollingPaperResponseDto(RollingPaper entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.theme = entity.getTheme();
        this.backgroundConfig = entity.getBackgroundConfig();
        this.messages = entity.getMessages().stream().map(MessageDto::new).collect(Collectors.toList());
        this.stickers = entity.getStickers().stream().map(StickerDto::new).collect(Collectors.toList());
    }

    @Getter
    public static class MessageDto {
        private Long id;
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

        public MessageDto(RollingPaperMessage entity) {
            this.id = entity.getId();
            this.content = entity.getContent();
            this.authorName = entity.getAuthorName();
            this.isAnonymous = entity.isAnonymous();
            this.backgroundColor = entity.getBackgroundColor();
            this.backgroundImage = entity.getBackgroundImage();
            this.fontFamily = entity.getFontFamily();
            this.fontColor = entity.getFontColor();
            this.textAlign = entity.getTextAlign();
            this.posX = entity.getPosX();
            this.posY = entity.getPosY();
            this.rotation = entity.getRotation();
            this.zIndex = entity.getZIndex();
        }
    }

    @Getter
    public static class StickerDto {
        private Long id;
        private String stickerUrl;
        private Double posX;
        private Double posY;
        private Double scale;
        private Double rotation;
        private Integer zIndex;

        public StickerDto(RollingPaperSticker entity) {
            this.id = entity.getId();
            this.stickerUrl = entity.getStickerUrl();
            this.posX = entity.getPosX();
            this.posY = entity.getPosY();
            this.scale = entity.getScale();
            this.rotation = entity.getRotation();
            this.zIndex = entity.getZIndex();
        }
    }
}

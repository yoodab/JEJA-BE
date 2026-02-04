package com.jeja.jejabe.rollingpaper.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class RollingPaperSticker extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rolling_paper_id")
    private RollingPaper rollingPaper;

    private String stickerUrl; // URL of the sticker image

    private Double posX;
    private Double posY;
    private Double scale;
    private Double rotation;
    private Integer zIndex;

    @Builder
    public RollingPaperSticker(RollingPaper rollingPaper, String stickerUrl, Double posX, Double posY, Double scale, Double rotation, Integer zIndex) {
        this.rollingPaper = rollingPaper;
        this.stickerUrl = stickerUrl;
        this.posX = posX;
        this.posY = posY;
        this.scale = scale;
        this.rotation = rotation;
        this.zIndex = zIndex;
    }
}

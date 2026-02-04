package com.jeja.jejabe.rollingpaper.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class RollingPaperMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rolling_paper_id")
    private RollingPaper rollingPaper;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String authorName;

    private boolean isAnonymous;

    // Style properties
    private String backgroundColor;
    private String backgroundImage; // Added for post-it background image
    private String fontFamily;
    private String fontColor;
    private String textAlign;

    // Position
    private Double posX;
    private Double posY;
    private Double rotation;

    private Integer zIndex;

    @Builder
    public RollingPaperMessage(RollingPaper rollingPaper, String content, String authorName, boolean isAnonymous,
                               String backgroundColor, String backgroundImage, String fontFamily, String fontColor, String textAlign,
                               Double posX, Double posY, Double rotation, Integer zIndex) {
        this.rollingPaper = rollingPaper;
        this.content = content;
        this.authorName = authorName;
        this.isAnonymous = isAnonymous;
        this.backgroundColor = backgroundColor;
        this.backgroundImage = backgroundImage;
        this.fontFamily = fontFamily;
        this.fontColor = fontColor;
        this.textAlign = textAlign;
        this.posX = posX;
        this.posY = posY;
        this.rotation = rotation;
        this.zIndex = zIndex;
    }
}

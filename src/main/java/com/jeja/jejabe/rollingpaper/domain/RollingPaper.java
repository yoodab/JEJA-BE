package com.jeja.jejabe.rollingpaper.domain;

import com.jeja.jejabe.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class RollingPaper extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String theme; // "BLACK", "LIGHT", "CUSTOM"

    @Column(columnDefinition = "TEXT")
    private String backgroundConfig; // JSON string for custom background (image url, gradient, effect)

    @OneToMany(mappedBy = "rollingPaper", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RollingPaperMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "rollingPaper", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RollingPaperSticker> stickers = new ArrayList<>();

    @Builder
    public RollingPaper(String title, String theme, String backgroundConfig) {
        this.title = title;
        this.theme = theme;
        this.backgroundConfig = backgroundConfig;
    }

    public void update(String title, String theme, String backgroundConfig) {
        this.title = title;
        this.theme = theme;
        this.backgroundConfig = backgroundConfig;
    }
}

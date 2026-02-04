package com.jeja.jejabe.rollingpaper.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "rolling_paper_themes")
public class RollingPaperTheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String themeConfig; // JSON: { type: 'CUSTOM', background: {...}, effect: {...}, banner: {...} }

    // Optional: if we want to track who created it, but for now assuming Admin only
}
